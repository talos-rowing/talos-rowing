package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

public class PrepareFileDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	protected boolean cancelled;
	private JProgressBar progressBar;

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
	
	void launch(File trsd) {
				
		File f;

		try {
			if (trsd.getName().endsWith(".trsd")) {
				f = uncimpressFile(trsd);
			} else {
				f = convertFileVersion(trsd);
			}

			onFinish(f);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	void cancel() {
		cancelled = true;
	}
	
	private File convertFileVersion(File input) throws Exception {
		
		DataVersionConverter converter = DataVersionConverter.getConvertersFor(input);

		if (converter != null) {

			converter.setProgressListener(new DataVersionConverter.ProgressListener() {

				@Override
				public boolean onProgress(double d) {

					progressBar.setValue((int)(100 * d));

					Thread.yield();

					return !cancelled;
				}
			});

			input = converter.convert(input);
		}
	
		return input;
	}
	
	private File uncimpressFile(File trsd) {
		
		try {
			

			File res = File.createTempFile("talos-rowing-data", ".txt");
			res.deleteOnExit();

			DataStreamCopier converter = new DataStreamCopier(
					new GZIPInputStream(new FileInputStream(trsd)), 
					new FileOutputStream(res), 
					trsd.length()) {
				
				@Override
				protected void onStart() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					
				}
				
				@Override
				protected boolean onProgress(double d) {
					
					Thread.yield();

					int pos = (int) (100.0 * d);

					progressBar.setValue(pos);
					
					return !cancelled;
				}
				
				@Override
				protected void onFinish() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}										
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

}
