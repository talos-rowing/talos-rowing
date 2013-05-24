package org.nargila.robostroke.oggz;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

public abstract class MergeTalosOggPanel extends JPanel {
	
	private JTextField inputOgg;
	private JTextField inputTalos;
	private JButton cancelBtn;
	private JButton startBtn;
	private JButton btnSelectOgg;
	private JButton btnSelectTalos;
	private JButton btnSelectOutput;
	private JTextField outputOgg;
	private JLabel statusLine;
	private JProgressBar progressBar;
	private JFileChooser fc;
	private File outfile;
	private File talos;
	private File ogg;
	private final AtomicReference<Exception> error = new AtomicReference<Exception>();
	

	private interface OggzStatusListener extends MergeTalosOgg.StatusListener {
		public void setCanceled(boolean canceled);
	}
	
	final OggzStatusListener oggzStatusListener = new OggzStatusListener() {
		
		private boolean canceled;
		private int counter;
		
		@Override
		public void onStatus(final String msg) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						statusLine.setText(msg);
						progressBar.setValue(++counter * 20);
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		public boolean isCanceled() {
			return canceled;
		}
		
		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}
		
	};
	
	/**
	 * Create the panel.
	 */
	public MergeTalosOggPanel() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		btnSelectOgg = new JButton("Select OGG");
		btnSelectOgg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseOggFile();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnSelectOgg, 38, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnSelectOgg, 21, SpringLayout.WEST, this);
		add(btnSelectOgg);
		
		inputOgg = new JTextField();
		inputOgg.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, inputOgg, 0, SpringLayout.NORTH, btnSelectOgg);
		springLayout.putConstraint(SpringLayout.WEST, inputOgg, 18, SpringLayout.EAST, btnSelectOgg);
		springLayout.putConstraint(SpringLayout.SOUTH, inputOgg, 0, SpringLayout.SOUTH, btnSelectOgg);
		springLayout.putConstraint(SpringLayout.EAST, inputOgg, -25, SpringLayout.EAST, this);
		add(inputOgg);
		inputOgg.setColumns(10);
		
		btnSelectTalos = new JButton("Select Talos");
		springLayout.putConstraint(SpringLayout.WEST, btnSelectTalos, 21, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, btnSelectTalos, -297, SpringLayout.EAST, this);
		btnSelectTalos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseTalosFile();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnSelectTalos, 21, SpringLayout.SOUTH, btnSelectOgg);
		springLayout.putConstraint(SpringLayout.EAST, btnSelectOgg, 0, SpringLayout.EAST, btnSelectTalos);
		add(btnSelectTalos);
		
		inputTalos = new JTextField();
		inputTalos.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, inputTalos, 0, SpringLayout.NORTH, btnSelectTalos);
		springLayout.putConstraint(SpringLayout.WEST, inputTalos, 18, SpringLayout.EAST, btnSelectTalos);
		springLayout.putConstraint(SpringLayout.SOUTH, inputTalos, 0, SpringLayout.SOUTH, btnSelectTalos);
		springLayout.putConstraint(SpringLayout.EAST, inputTalos, -25, SpringLayout.EAST, this);
		add(inputTalos);
		inputTalos.setColumns(10);
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, cancelBtn, -64, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, cancelBtn, -31, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, cancelBtn, -269, SpringLayout.EAST, this);
		add(cancelBtn);
		
		startBtn = new JButton("Start");
		startBtn.setEnabled(false);
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStart();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, startBtn, 0, SpringLayout.NORTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.WEST, startBtn, -148, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, startBtn, 0, SpringLayout.SOUTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.EAST, startBtn, -67, SpringLayout.EAST, this);
		add(startBtn);
		
		btnSelectOutput = new JButton("Output");
		btnSelectOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseOutputFile();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnSelectOutput, 21, SpringLayout.SOUTH, btnSelectTalos);
		springLayout.putConstraint(SpringLayout.WEST, btnSelectOutput, 21, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, btnSelectOutput, 0, SpringLayout.EAST, btnSelectOgg);
		add(btnSelectOutput);
		
		outputOgg = new JTextField();
		outputOgg.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, outputOgg, 0, SpringLayout.NORTH, btnSelectOutput);
		springLayout.putConstraint(SpringLayout.WEST, outputOgg, 18, SpringLayout.EAST, btnSelectOutput);
		springLayout.putConstraint(SpringLayout.SOUTH, outputOgg, 0, SpringLayout.SOUTH, btnSelectOutput);
		springLayout.putConstraint(SpringLayout.EAST, outputOgg, -25, SpringLayout.EAST, this);
		add(outputOgg);
		outputOgg.setColumns(10);
		
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		springLayout.putConstraint(SpringLayout.NORTH, progressBar, -47, SpringLayout.NORTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.WEST, progressBar, 21, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, progressBar, -22, SpringLayout.NORTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.EAST, progressBar, -25, SpringLayout.EAST, this);
		add(progressBar);
		
		statusLine = new JLabel("");
		statusLine.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, statusLine, 0, SpringLayout.WEST, btnSelectOgg);
		springLayout.putConstraint(SpringLayout.SOUTH, statusLine, -6, SpringLayout.NORTH, progressBar);
		springLayout.putConstraint(SpringLayout.EAST, statusLine, 0, SpringLayout.EAST, inputOgg);
		add(statusLine);

	}

	private void cancel() {
		if (oggzStatusListener != null) {
			oggzStatusListener.setCanceled(true);
		}
		
		onClose();
	}

	private void onStart() {
		
		btnSelectOgg.setEnabled(false);
		btnSelectTalos.setEnabled(false);
		btnSelectOutput.setEnabled(false);
		
		progressBar.setVisible(true);
		
		startBtn.setEnabled(false);
		
		final MergeTalosOggPanel self = this;
		
		new Thread("MergeTalosOgg") {
			public void run() {
				MergeTalosOgg oggz = new MergeTalosOgg(ogg, talos, outfile);
				try {
					oggz.process(oggzStatusListener);
				} catch (Exception e) {
					error.set(e);
				} finally {

					if (!oggzStatusListener.isCanceled() && error.get() != null) {
						JOptionPane.showMessageDialog(self, error.get().getMessage(), error.get().getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
					} else {					
						oggzStatusListener.onStatus("Done.");
					}
					
					EventQueue.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							cancelBtn.setText("Close");
							cancelBtn.setSelected(true);
						}
					});
				}

			}
		}.start();			
		
	}

	protected abstract void onClose();
	
	private File chooseFile(FileFilter fileFilter, boolean saveFile) {
		
		if (fc == null) {
			fc = new JFileChooser();
		}
		
		fc.setFileFilter(fileFilter);
		
		
		int status = saveFile ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
		
		
		return JFileChooser.APPROVE_OPTION == status ? fc.getSelectedFile() : null;
	}


	protected void chooseOutputFile() {
		
		outfile = chooseFile(new FileFilter() {
			
			@Override
			public String getDescription() {				
				return "OGG Video files";
			}
			
			@Override
			public boolean accept(File f) {
				
				if (f.isDirectory()) {
					return true;
				} else {
					String name = f.getName();
					return name.endsWith(".ogg");
				}
			}
		}, true);
		
				
		if (outfile != null) {
			outputOgg.setText(outfile.getAbsolutePath());
		}
		
		updateState();
	}

	protected void chooseTalosFile() {
		
		talos = chooseFile(new FileFilter() {
			
			@Override
			public String getDescription() {				
				return "Talos Rowing Data Files";
			}
			
			@Override
			public boolean accept(File f) {
				
				if (f.isDirectory()) {
					return true;
				} else {
					String name = f.getName();
					return name.endsWith(".trsd") || name.endsWith(".txt");
				}
			}
		}, false);
		
		if (talos != null) {
			inputTalos.setText(talos.getAbsolutePath());
		}
		
		updateState();
	}

	private void updateState() {
		boolean enable = ogg != null && talos != null && outfile != null;
		startBtn.setEnabled(enable);

		if (enable) {
			cancelBtn.setSelected(true);
		}
	}

	protected void chooseOggFile() {
		ogg = chooseFile(new FileFilter() {
			
			@Override
			public String getDescription() {				
				return "OGG Video files";
			}
			
			@Override
			public boolean accept(File f) {
				
				if (f.isDirectory()) {
					return true;
				} else {
					String name = f.getName();
					return name.endsWith(".ogg");
				}
			}
		}, false);	

		if (ogg != null) {
			inputOgg.setText(ogg.getAbsolutePath());
		}	
		updateState();
	}

}
