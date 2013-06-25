package org.nargila.robostroke.oggz;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.SynchedFileDataInput;
import org.nargila.robostroke.data.media.MediaSynchedFileDataInput;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;

public abstract class SetupExternalMediaInfoPanel extends JPanel {
	
	private JTextField inputOgg;
	private JTextField inputTalos;
	private JButton cancelBtn;
	private JButton saveBtn;
	private JButton btnSelectMedia;
	private JButton btnSelectTalos;
	private JButton btnSelectOutput;
	private JTextField outputOgg;
	private JLabel statusLine;
	private JProgressBar progressBar;
	private JFileChooser fc;
	private File outfile;
	private File talosFile;
	private File mediaFile;
	private final AtomicReference<Exception> error = new AtomicReference<Exception>();

	private JFormattedTextField textTimeOffset;
	private JFormattedTextField textMarkId;
	private JButton btnDetect;
	private GstFindQrMarkPipeline findQr;
	private boolean canceled;
	private Pair<Integer, Long> syncData;
	
	/**
	 * Create the panel.
	 */
	public SetupExternalMediaInfoPanel() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		btnSelectMedia = new JButton("Select Media");
		btnSelectMedia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseMediaFile();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnSelectMedia, 38, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnSelectMedia, 21, SpringLayout.WEST, this);
		add(btnSelectMedia);
		
		inputOgg = new JTextField();
		inputOgg.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, inputOgg, 0, SpringLayout.NORTH, btnSelectMedia);
		springLayout.putConstraint(SpringLayout.WEST, inputOgg, 18, SpringLayout.EAST, btnSelectMedia);
		springLayout.putConstraint(SpringLayout.SOUTH, inputOgg, 0, SpringLayout.SOUTH, btnSelectMedia);
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
		springLayout.putConstraint(SpringLayout.NORTH, btnSelectTalos, 21, SpringLayout.SOUTH, btnSelectMedia);
		springLayout.putConstraint(SpringLayout.EAST, btnSelectMedia, 0, SpringLayout.EAST, btnSelectTalos);
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
		
		saveBtn = new JButton("Save");
		saveBtn.setEnabled(false);
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSave();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, saveBtn, 0, SpringLayout.NORTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.WEST, saveBtn, -148, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, saveBtn, 0, SpringLayout.SOUTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.EAST, saveBtn, -67, SpringLayout.EAST, this);
		add(saveBtn);
		
		btnSelectOutput = new JButton("Output");
		btnSelectOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseOutputFile();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnSelectOutput, 21, SpringLayout.SOUTH, btnSelectTalos);
		springLayout.putConstraint(SpringLayout.WEST, btnSelectOutput, 21, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, btnSelectOutput, 0, SpringLayout.EAST, btnSelectMedia);
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
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		springLayout.putConstraint(SpringLayout.NORTH, progressBar, -47, SpringLayout.NORTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.WEST, progressBar, 21, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, progressBar, -22, SpringLayout.NORTH, cancelBtn);
		springLayout.putConstraint(SpringLayout.EAST, progressBar, -25, SpringLayout.EAST, this);
		add(progressBar);
		
		statusLine = new JLabel("");
		statusLine.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, statusLine, 0, SpringLayout.WEST, btnSelectMedia);
		springLayout.putConstraint(SpringLayout.SOUTH, statusLine, -6, SpringLayout.NORTH, progressBar);
		springLayout.putConstraint(SpringLayout.EAST, statusLine, 0, SpringLayout.EAST, inputOgg);
		add(statusLine);
		
		JLabel lblDataStartTime = new JLabel("Data Start Time Offset");
		springLayout.putConstraint(SpringLayout.NORTH, lblDataStartTime, 35, SpringLayout.SOUTH, btnSelectOutput);
		springLayout.putConstraint(SpringLayout.WEST, lblDataStartTime, 0, SpringLayout.WEST, btnSelectMedia);
		add(lblDataStartTime);
		
		JLabel lblDataStartSynch = new JLabel("Data Start Synch Mark ID");
		springLayout.putConstraint(SpringLayout.WEST, lblDataStartSynch, 0, SpringLayout.WEST, btnSelectMedia);
		add(lblDataStartSynch);
		
		textTimeOffset = new JFormattedTextField();
		textTimeOffset.setValue(-1L);
		springLayout.putConstraint(SpringLayout.NORTH, lblDataStartSynch, 6, SpringLayout.SOUTH, textTimeOffset);
		springLayout.putConstraint(SpringLayout.NORTH, textTimeOffset, 6, SpringLayout.SOUTH, lblDataStartTime);
		springLayout.putConstraint(SpringLayout.WEST, textTimeOffset, 21, SpringLayout.WEST, this);
		add(textTimeOffset);
		textTimeOffset.setColumns(10);
		
		textMarkId = new JFormattedTextField();
		textMarkId.setValue(-1);
		springLayout.putConstraint(SpringLayout.EAST, textTimeOffset, 0, SpringLayout.EAST, textMarkId);
		springLayout.putConstraint(SpringLayout.NORTH, textMarkId, 6, SpringLayout.SOUTH, lblDataStartSynch);
		springLayout.putConstraint(SpringLayout.WEST, textMarkId, 0, SpringLayout.WEST, btnSelectMedia);
		springLayout.putConstraint(SpringLayout.EAST, textMarkId, -246, SpringLayout.EAST, this);
		add(textMarkId);
		textMarkId.setColumns(10);
		
		textTimeOffset.getDocument().addDocumentListener(new DigitDocumentListener(textTimeOffset));
		textMarkId.getDocument().addDocumentListener(new DigitDocumentListener(textMarkId));

		btnDetect = new JButton("Detect");
		btnDetect.setEnabled(false);
		btnDetect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDetect();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnDetect, 0, SpringLayout.NORTH, textTimeOffset);
		springLayout.putConstraint(SpringLayout.WEST, btnDetect, 24, SpringLayout.EAST, textTimeOffset);
		springLayout.putConstraint(SpringLayout.SOUTH, btnDetect, 0, SpringLayout.SOUTH, textMarkId);
		springLayout.putConstraint(SpringLayout.EAST, btnDetect, 136, SpringLayout.EAST, textTimeOffset);
		add(btnDetect);

	}

	private void cancel() {
		
		canceled = true;
		
		if (findQr != null) {
			findQr.stop();
		}
		
		onClose();
	}

	private void onDetect() {
		
		textTimeOffset.setEditable(false);
		textMarkId.setEditable(false);
		
		btnSelectMedia.setEnabled(false);
		btnSelectTalos.setEnabled(false);
		btnSelectOutput.setEnabled(false);
		
		progressBar.setVisible(true);
		
		saveBtn.setEnabled(false);
		
		final SetupExternalMediaInfoPanel self = this;

		findQr = new GstFindQrMarkPipeline(mediaFile);
		
		new Thread("DetectQrMark") {
			public void run() {
				
				Pair<Integer, Long> res = null;
				
				try {					
					res = findQr.findMark(60);
				} catch (Exception e) {
					error.set(e);
				} finally {

					if (!canceled && error.get() != null) {
						JOptionPane.showMessageDialog(self, error.get().getMessage(), error.get().getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
					}
					
					final Pair<Integer, Long> resFinal = res;
					
					EventQueue.invokeLater(new Runnable() {

						@Override
						public void run() {
							progressBar.setVisible(false);
							textTimeOffset.setEditable(true);
							textMarkId.setEditable(true);
							btnSelectMedia.setEnabled(true);
							btnSelectTalos.setEnabled(true);
							btnSelectOutput.setEnabled(true);
							cancelBtn.setSelected(true);
							
							if (resFinal != null) {
								textTimeOffset.setText(resFinal.second + "");
								textMarkId.setText(resFinal.first + "");
							}
						}
					});
				}
			}
		}.start();			
		
	}

	protected void setSynchData(final Pair<Integer, Long> res) {
		if (res != null) {
			this.syncData = res;
		}
		
		updateState();
	}

	private void onSave() {

		Properties props = new Properties();

		props.setProperty(MediaSynchedFileDataInput.PROP_SYCH_MARK_ID, syncData.first.toString());
		props.setProperty(MediaSynchedFileDataInput.PROP_TIME_OFFSET, syncData.second.toString());
		props.setProperty(MediaSynchedFileDataInput.PROP_TALOS_DATA, talosFile.getAbsolutePath());		

		try {
			props.store(new FileWriter(outfile), "");
		} catch (Exception e) {					
			JOptionPane.showMessageDialog(this, error.get().getMessage(), error.get().getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}

		onClose();

	}

	protected abstract void onClose();
	
	private String chooseFile(FileFilter fileFilter, boolean saveFile, File suggestFile) {
		
		if (fc == null) {
			fc = new JFileChooser();
		}
		
		fc.setFileFilter(fileFilter);
				
		fc.setSelectedFile(suggestFile);

		int status = saveFile ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
						
		return JFileChooser.APPROVE_OPTION == status ? fc.getSelectedFile().getAbsolutePath() : null;
	}


	protected void chooseOutputFile() {
		
		File suggestedFile = mediaFile == null ? null : new File(mediaFile.getAbsolutePath() + ".talos.properties");
		
		String path = chooseFile(TALOS_MEDIA_INFO_FILES_FILTER, true, suggestedFile);		
				
		outfile = setupFilePath(path, outputOgg, false);
		updateState();
	}

	private void chooseTalosFile() {
		
		String path = chooseFile(new FileFilter() {
			
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
		}, false, null);
		
		talosFile = setupFilePath(path, inputTalos, true);
		
		updateState();
	}

	private File setupFilePath(String path, JTextComponent text, boolean checkExists) {
		
		if (path != null) {
			File res = new File(path);
			if (!checkExists || res.exists()) {
				text.setText(res.getAbsolutePath());
				return res;
			}
		}
		
		return null;		
	}
	
	private void updateState() {
		
		boolean enable = mediaFile != null && talosFile != null && outfile != null && syncData != null;

		btnDetect.setEnabled(mediaFile != null);
		
		saveBtn.setEnabled(enable);

		if (enable) {
			cancelBtn.setSelected(true);
		}
	}

	protected void chooseMediaFile() {

		String path = chooseFile(ALL_FILES_FILTER, false, null);	

		mediaFile = setupFilePath(path, inputOgg, true);
		
		if (talosFile == null) {
			talosFile = setupFilePath(mediaFile.getAbsolutePath().replaceFirst("\\.[a-zA-Z0-9]+$", ".txt"), inputTalos, true);
		}
		
		if (outfile == null) {
			outfile = setupFilePath(mediaFile.getAbsolutePath() + ".talos.properties", outputOgg, false);
		}
		
		updateState();
	}
	
	private static final FileFilter ALL_FILES_FILTER = new FileFilter() {
		
		@Override
		public String getDescription() {				
			return "All files";
		}
		
		@Override
		public boolean accept(File f) {
			
			if (f.isDirectory()) {
				return true;
			} else {
				return true;
			}
		}
	};
	
	private static final FileFilter TALOS_MEDIA_INFO_FILES_FILTER = new FileFilter() {
		
		@Override
		public String getDescription() {				
			return "Talos Media Info files";
		}
		
		@Override
		public boolean accept(File f) {
			
			if (f.isDirectory()) {
				return true;
			} else {
				return f.getName().endsWith(".talos.properties");
			}
		}
	};
	
	
	private class DigitDocumentListener implements DocumentListener {
		
		final JTextComponent text;
		String lastValue;
		
		DigitDocumentListener(JTextComponent text) {
			this.text = text;
		}
		
		public void changedUpdate(DocumentEvent e) {
			check();				
		}
		public void removeUpdate(DocumentEvent e) {
			check();				
		}
		public void insertUpdate(DocumentEvent e) {
			check();				
		}

		public void check() {	
			
			String value = text.getText();
			if (value.equals(lastValue)) {
				return;				
			}

			lastValue = value;
			
			try {
				
				new Long(value);
				
				try {
					
					long time = (Long) textTimeOffset.getValue();
					
					int markId = (Integer)textMarkId.getValue();
					
					if (time > 0 && markId > 0) {
						setSynchData(Pair.create(markId, time));
					}
				} catch (NumberFormatException e) {
				}
			} catch (NumberFormatException e) {				
			}
		}
	}
	
}
