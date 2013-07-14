package org.nargila.robostroke.media;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.nargila.robostroke.app.Settings;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.media.ExternalMedia.VideoEffect;
import org.nargila.robostroke.data.media.MediaSynchedFileDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class SetupExternalMediaInfoPanel extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(SetupExternalMediaInfoPanel.class);
	private final JProgressBar progressBar;
	private JFileChooser fc;
	private final AtomicReference<File> mediaFile = new AtomicReference<File>();
	
	private final AtomicReference<VideoEffect> videoEffect = new AtomicReference<VideoEffect>(VideoEffect.NONE);
    private final AtomicReference<Exception> error = new AtomicReference<Exception>();
	
	private FindQrMarkPipeline findQr;
	private final AtomicReference<Pair<Integer, Long>> syncData = new AtomicReference<Pair<Integer,Long>>();
    private Settings pref;
	
    boolean canceled = false;
    boolean modified = false;
    
	/**
	 * Create the panel.
	 */
	public SetupExternalMediaInfoPanel() {
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 175, 0, 66, 0, 1, 0};
		gbl_panel.rowHeights = new int[]{26, 20, 26, 0, 0, 0, 0, 39, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 5;
		gbc_label.gridy = 0;
		panel.add(label, gbc_label);
		
		btnSelectMedia = new JButton("Select Media");
		btnSelectMedia.addActionListener(new ActionListener() {
		    @Override
            public void actionPerformed(ActionEvent e) {
		        chooseMediaFile();
		    }
		});
		GridBagConstraints gbc_btnSelectMedia = new GridBagConstraints();
		gbc_btnSelectMedia.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnSelectMedia.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectMedia.gridx = 1;
		gbc_btnSelectMedia.gridy = 1;
		panel.add(btnSelectMedia, gbc_btnSelectMedia);
		
		lblDelete = new JLabel("X");
		lblDelete.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        onDeleteMedia();
		    }
		});
		lblDelete.setForeground(new Color(255, 0, 0));
		GridBagConstraints gbc_lblDelete = new GridBagConstraints();
		gbc_lblDelete.insets = new Insets(0, 0, 5, 5);
		gbc_lblDelete.anchor = GridBagConstraints.EAST;
		gbc_lblDelete.gridx = 2;
		gbc_lblDelete.gridy = 1;
		panel.add(lblDelete, gbc_lblDelete);
		
		inputMedia = new JTextField();
		inputMedia.setEditable(false);
		inputMedia.setColumns(10);
		GridBagConstraints gbc_inputOgg = new GridBagConstraints();
		gbc_inputOgg.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputOgg.insets = new Insets(0, 0, 5, 5);
		gbc_inputOgg.gridx = 3;
		gbc_inputOgg.gridy = 1;
		panel.add(inputMedia, gbc_inputOgg);
		
		lblVideoEvects = new JLabel("Video Evects");
		lblVideoEvects.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVideoEvects.setEnabled(false);
		GridBagConstraints gbc_lblVideoEvects = new GridBagConstraints();
		gbc_lblVideoEvects.anchor = GridBagConstraints.EAST;
		gbc_lblVideoEvects.insets = new Insets(0, 0, 5, 5);
		gbc_lblVideoEvects.gridx = 1;
		gbc_lblVideoEvects.gridy = 2;
		panel.add(lblVideoEvects, gbc_lblVideoEvects);
		
		cbxVideoEfects = new JComboBox();
		cbxVideoEfects.addItemListener(new ItemListener() {
		    @Override
            public void itemStateChanged(ItemEvent e) {
		        setValue(videoEffect, (VideoEffect) e.getItem());
		        updateState();
		    }
		});
		
		cbxVideoEfects.setEnabled(false);
		GridBagConstraints gbc_cbxVideoEfects = new GridBagConstraints();
		gbc_cbxVideoEfects.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbxVideoEfects.anchor = GridBagConstraints.NORTH;
		gbc_cbxVideoEfects.insets = new Insets(0, 0, 5, 5);
		gbc_cbxVideoEfects.gridx = 3;
		gbc_cbxVideoEfects.gridy = 2;
		panel.add(cbxVideoEfects, gbc_cbxVideoEfects);
		
		label_2 = new JLabel("Data Start Synch Mark ID");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.EAST;
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 1;
		gbc_label_2.gridy = 4;
		panel.add(label_2, gbc_label_2);
		
		textMarkId = new JFormattedTextField();
		textMarkId.setColumns(10);
		textMarkId.setValue(1);
		textMarkId.addFocusListener(new FocusAdapter() {
		    @Override
		    public void focusGained(FocusEvent e) {
		        EventQueue.invokeLater(new Runnable() {
		            @Override
                    public void run() {
		                textMarkId.selectAll();                 
		            }
		        });
		    }
		    @Override
		    public void focusLost(FocusEvent e) {
		        setSynchData(Pair.create((Integer)textMarkId.getValue(), (Long)textTimeOffset.getValue()));
		    }
		});
		GridBagConstraints gbc_textMarkId = new GridBagConstraints();
		gbc_textMarkId.fill = GridBagConstraints.HORIZONTAL;
		gbc_textMarkId.anchor = GridBagConstraints.NORTH;
		gbc_textMarkId.insets = new Insets(0, 0, 5, 5);
		gbc_textMarkId.gridx = 3;
		gbc_textMarkId.gridy = 4;
		panel.add(textMarkId, gbc_textMarkId);
		
		label_1 = new JLabel("Data Start Time Offset");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 5;
		panel.add(label_1, gbc_label_1);
		
		textTimeOffset = new JFormattedTextField();
		textTimeOffset.setColumns(10);
		textTimeOffset.setValue(0L);
        textTimeOffset.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textTimeOffset.selectAll();                 
                    }
                });
            }
            @Override
            public void focusLost(FocusEvent e) {
                setSynchData(Pair.create((Integer)textMarkId.getValue(), (Long)textTimeOffset.getValue()));
            }
        });
        
		GridBagConstraints gbc_textTimeOffset = new GridBagConstraints();
		gbc_textTimeOffset.fill = GridBagConstraints.HORIZONTAL;
		gbc_textTimeOffset.anchor = GridBagConstraints.NORTH;
		gbc_textTimeOffset.insets = new Insets(0, 0, 5, 5);
		gbc_textTimeOffset.gridx = 3;
		gbc_textTimeOffset.gridy = 5;
		panel.add(textTimeOffset, gbc_textTimeOffset);
		
		btnDetect = new JButton("Find Sync Mark");
		btnDetect.addActionListener(new ActionListener() {
		    @Override
            public void actionPerformed(ActionEvent e) {
		        onDetect();
		    }
		});
		btnDetect.setEnabled(false);
		GridBagConstraints gbc_btnDetect = new GridBagConstraints();
		gbc_btnDetect.anchor = GridBagConstraints.EAST;
		gbc_btnDetect.insets = new Insets(0, 0, 5, 5);
		gbc_btnDetect.gridx = 1;
		gbc_btnDetect.gridy = 7;
		panel.add(btnDetect, gbc_btnDetect);
		
		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar.gridx = 3;
		gbc_progressBar.gridy = 7;
		panel.add(progressBar, gbc_progressBar);
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		
		horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBorder(new EmptyBorder(20, 0, 20, 0));
		add(horizontalBox, BorderLayout.SOUTH);
		
		horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
		    @Override
            public void actionPerformed(ActionEvent e) {
		        cancel();
		    }
		});
		horizontalBox.add(cancelBtn);
		
		horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_1);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
		    @Override
            public void actionPerformed(ActionEvent e) {
		        onSave();
		    }
		});
		btnSave.setEnabled(false);
		horizontalBox.add(btnSave);
		
		horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_2);
		
		for (VideoEffect e: VideoEffect.values()) {
			cbxVideoEfects.addItem(e);
		}
	}

	protected void onDeleteMedia() {	    
	    reset();
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
        
        progressBar.setVisible(true);
        
        btnSave.setEnabled(false);
        
        final SetupExternalMediaInfoPanel self = this;

        try {
            findQr = MediaPlayerFactory.createFindQrMarkPipeline(mediaFile.get());
        } catch (Exception e) {
            logger.error("error creating Qr mark pipeline", e);
            error.set(e);
        }

        new Thread("DetectQrMark") {
            @Override
            public void run() {
                
                final AtomicReference<Pair<Integer, Long>> res = new AtomicReference<Pair<Integer,Long>>();
                
                try { 
                    if (error.get() == null) {
                        res.set(findQr.findMark(60));
                    }
                } catch (Exception e) {
                    error.set(e);
                } finally {

                    if (!canceled && error.get() != null) {
                        JOptionPane.showMessageDialog(self, error.get().getMessage(), error.get().getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                    }
                                                            
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            progressBar.setVisible(false);
                            textTimeOffset.setEditable(true);
                            textMarkId.setEditable(true);
                            btnSelectMedia.setEnabled(true);
                            cancelBtn.setSelected(true);
                            
                            if (res.get() != null) {
                                setSynchData(res.get());
                            }
                        }
                    });
                }
            }
        }.start();          
        
    }

	private void onSave() {

	    pref.put(MediaSynchedFileDataInput.PROP_SYCH_MARK_ID, syncData.get().first);
	    pref.put(MediaSynchedFileDataInput.PROP_TIME_OFFSET, syncData.get().second);
	    pref.put(MediaSynchedFileDataInput.PROP_MEDIA_FILE, mediaFile.get() == null ? null : mediaFile.get().getAbsolutePath());        
	    pref.put(MediaSynchedFileDataInput.PROP_VIDEO_EFFECT, videoEffect.get().name());     

	    onClose();

	}

	private void setSynchData(final Pair<Integer, Long> res) {
		
		if (res != null && res.first >= -1 && res.second > -1) {
		    setValue(syncData,  res);
		} else {
            setValue(syncData,  null);
		}
		
		textMarkId.setValue(syncData.get() == null ? 1 : syncData.get().first);
		textTimeOffset.setValue(syncData.get() == null ? 0L : syncData.get().second);
		
		updateState();
	}

	protected abstract void onClose();
	
	private void chooseMediaFile() {

	    String path = chooseFile(ALL_FILES_FILTER, false, null);    

	    setPathItem(path, mediaFile, inputMedia, true);

	}
	   
	private String chooseFile(FileFilter fileFilter, boolean saveFile, File suggestFile) {
		
		if (fc == null) {
			fc = new JFileChooser(Settings.getInstance().getLastDir());
		}
		
		fc.setFileFilter(fileFilter);
				
		fc.setSelectedFile(suggestFile);

		int status = saveFile ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
						
		if (JFileChooser.APPROVE_OPTION == status) {
			File f = fc.getSelectedFile();
			Settings.getInstance().setLastDir(f.getParentFile());
			return f.getAbsolutePath();
		}
		
		return null;
	}

	private <T> void setValue(AtomicReference<T> var, T val) {
	    
	    if (var.get() == null) {
	        modified = val != null;
	    } else {
	        modified = !var.get().equals(val);
	    }
	    
	    var.set(val);
	}
	
	private void setPathItem(String path, AtomicReference<File> value, JTextComponent text, boolean checkExists) {
		
	    File res = null;
	    
		if (path != null) {
			res = new File(path);
			
			if (checkExists && !res.exists()) {
			    res = null;
			}
		}
		
		text.setText(res == null ? "" : res.getAbsolutePath());
		setValue(value, res);
		updateState();
	}
	
	private void reset() {
	    setPathItem(null, mediaFile, inputMedia, false);
	    setSynchData(Pair.create(1, 0L));
	    cbxVideoEfects.setSelectedItem(VideoEffect.NONE);
	    updateState();
	}
	
	private void updateState() {
		
		boolean haveMedia = mediaFile.get() != null;
		boolean haveSyncData = syncData.get() != null;
		boolean enable = haveMedia;

		logger.info("can save: {} (mediaFile: {}, syncData: {})", new Object[]{enable, haveMedia,haveSyncData});

		btnDetect.setEnabled(haveMedia);
		lblVideoEvects.setEnabled(haveMedia);
		cbxVideoEfects.setEnabled(haveMedia);
        lblDelete.setVisible(haveMedia);

		btnSave.setEnabled(modified);

		if (enable) {
			cancelBtn.setSelected(true);
		}
	}
	
	public void loadSettings(Settings pref, long time) {

	    this.pref = pref;
	    
	    cbxVideoEfects.setSelectedItem(VideoEffect.valueOf(pref.get(MediaSynchedFileDataInput.PROP_VIDEO_EFFECT, VideoEffect.NONE.name())));
	    setPathItem(pref.get(MediaSynchedFileDataInput.PROP_MEDIA_FILE, (String)null), mediaFile, inputMedia, true);	    
	    modified = false;	    
	    updateState();
	    setSynchData(Pair.create(pref.get(MediaSynchedFileDataInput.PROP_SYCH_MARK_ID, 1), time));
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
	
	private final Box horizontalBox;
	private final JButton cancelBtn;
	private final JButton btnSave;
	private final JPanel panel;
	private final JButton btnSelectMedia;
	private final JTextField inputMedia;
	private final JLabel label;
	private final JLabel label_1;
	private final JLabel label_2;
	private final JFormattedTextField textMarkId;
	private final JFormattedTextField textTimeOffset;
	private final JButton btnDetect;
	private final JLabel lblVideoEvects;
	private final JComboBox cbxVideoEfects;
	private final Component horizontalGlue;
	private final Component horizontalGlue_1;
	private final Component horizontalGlue_2;
	private final JLabel lblDelete;
    public JButton getButton_2() {
        return btnSelectMedia;
    }
}
