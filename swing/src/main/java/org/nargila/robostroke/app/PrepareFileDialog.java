/*
 * Copyright (c) 2012 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import org.nargila.robostroke.common.DataStreamCopier;
import org.nargila.robostroke.data.version.DataVersionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PrepareFileDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(PrepareFileDialog.class);
    
	private final JPanel contentPanel = new JPanel();
	protected boolean cancelled;
	private JProgressBar progressBar;

	private final AtomicBoolean showRequested = new AtomicBoolean();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PrepareFileDialog dialog = new PrepareFileDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public PrepareFileDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Prepare File");
		setBounds(100, 100, 450, 119);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			Component verticalGlue = Box.createVerticalGlue();
			contentPanel.add(verticalGlue);
		}
		{
			progressBar = new JProgressBar();
			contentPanel.add(progressBar);
		}
		{
			Component verticalGlue = Box.createVerticalGlue();
			contentPanel.add(verticalGlue);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			{
				Component horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
			{
				Component horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
		}
	}
	
	
    void launch(final File trsd) {
        
        logger.info("deffered launching of {} conversion/decompression", trsd);

        new Thread("RoboStrokeAppPanel prepareFile") {

            @Override
            public void run() {
                reallyLaunch(trsd);
            }
        }.start();

        synchronized (showRequested) {
            try {
                showRequested.wait();
                
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }
        
        if (showRequested.get() && !cancelled) {
            setVisible(true);
        }
        
    }        
	
	void reallyLaunch(File trsd) {				

        logger.info("real launching of {} conversion/decompression", trsd);

        File f = null;

		try {
			if (trsd.getName().endsWith(".trsd")) {
				f = uncompressFile(trsd);
			} else {
				f = convertFileVersion(trsd);
			}

		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    setVisible(false);
		    cancel();
		    onFinish(f);		    
		}
		
	}
		
	void cancel() {
		cancelled = true;
		
		synchronized (showRequested) {
		    showRequested.set(false);
		    showRequested.notifyAll();
		}
	}
	
	private File convertFileVersion(File input) throws Exception {
		
		DataVersionConverter converter = DataVersionConverter.getConvertersFor(input);

		if (converter != null) {

			converter.setProgressListener(new DataVersionConverter.ProgressListener() {

				@Override
				public boolean onProgress(double d) {

				    verifyVisibility(d);
				    
					progressBar.setValue((int)(100 * d));

					Thread.yield();

					return !cancelled;
				}
			});

			input = converter.convert(input);
		}
	
		return input;
	}
	
	private File uncompressFile(File trsd) {
		
        logger.info("uncompressing {}", trsd);

        try {
			

			File res = File.createTempFile("talos-rowing-data", ".txt");
			res.deleteOnExit();

			@SuppressWarnings("resource")
            DataStreamCopier converter = new DataStreamCopier(
					new GZIPInputStream(new FileInputStream(trsd)), 
					new FileOutputStream(res), 
					trsd.length()) {
				
				@Override
				protected boolean onProgress(double d) {
					
				    verifyVisibility(d);
				    
					Thread.yield();

					int pos = (int) (100.0 * d);

					logger.info("setting progressBar to {}", pos);

					progressBar.setValue(pos);
					

					return !cancelled;
				}
				
				@Override
				protected void onError(Exception e) {
					e.printStackTrace();
				}
			};
			
			converter.run();

			if (converter.isGood()) {
				return convertFileVersion(res);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	protected void onFinish(File f) {
		
	}

    private void verifyVisibility(double progress) {
        boolean justShown = false;

        synchronized (showRequested) {
            if (!cancelled && !isVisible() && progress < 1.0) {
                showRequested.set(true);
                showRequested.notifyAll();
                justShown = true;
            }
        }
        
        if (justShown) { // ensure progress is shown for one second insteed of a possible mere flicker on screen
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
