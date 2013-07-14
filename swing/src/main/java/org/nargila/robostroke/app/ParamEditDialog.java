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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.data.media.ExternalMedia;
import org.nargila.robostroke.data.media.ExternalMedia.MediaFramework;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.runtime.RuntimeUtil;

@SuppressWarnings("serial")
public class ParamEditDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(ParamEditDialog.class);

    private ParameterService ps;

	private final HashMap<String, JTextField> paramEditorsMap = new HashMap<String, JTextField>();
	private JPanel gridPanel;
	private JTextField txtVlcPath;
	private JTextField txtGstPath;
	private JRadioButton radioUseGst;
	private JRadioButton radioUseVlc;
	private JButton btnClearVlcPath;
	private JButton btnClearGstPath;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton btnVlcPath;
	private JButton btnGstPath;
	private JRadioButton radioUseJgst;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ParamEditDialog dialog = new ParamEditDialog();
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ParamEditDialog() {
		setTitle("Parameters");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 450, 300);
        
		getContentPane().setLayout(new BorderLayout());
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			{
				JPanel panel = new JPanel();
				tabbedPane.addTab("Settings", null, panel, null);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					btnVlcPath = new JButton("VLC Path");
					btnVlcPath.setEnabled(false);
					btnVlcPath.addActionListener(new ActionListener() {
						@Override
                        public void actionPerformed(ActionEvent e) {
							onFrameworkPath(MediaFramework.VLC);
						}
					});
					

					{
					    radioUseVlc = new JRadioButton("VLC");
					    buttonGroup.add(radioUseVlc);
					    radioUseVlc.setSelected(true);
					    
				        radioUseVlc.addItemListener(new ItemListener() {
				            @Override
				            public void itemStateChanged(ItemEvent e) {
				                if (radioUseVlc.isSelected()) {
				                    onFrameWorkSelected(MediaFramework.VLC);
				                }
				            }
				        });
				        
					    GridBagConstraints gbc_radioUseVlc = new GridBagConstraints();
					    gbc_radioUseVlc.anchor = GridBagConstraints.WEST;
					    gbc_radioUseVlc.insets = new Insets(0, 0, 5, 5);
					    gbc_radioUseVlc.gridx = 1;
					    gbc_radioUseVlc.gridy = 1;
					    panel.add(radioUseVlc, gbc_radioUseVlc);
					}
					GridBagConstraints gbc_btnVlcPath = new GridBagConstraints();
					gbc_btnVlcPath.fill = GridBagConstraints.HORIZONTAL;
					gbc_btnVlcPath.insets = new Insets(0, 0, 5, 5);
					gbc_btnVlcPath.gridx = 2;
					gbc_btnVlcPath.gridy = 1;
					panel.add(btnVlcPath, gbc_btnVlcPath);
				}
				{
				    File dir = Settings.getInstance().getMediaFrameworkNativeDir(MediaFramework.VLC);
					txtVlcPath = new JTextField(dir == null ? "" : dir.getAbsolutePath());
					txtVlcPath.setEditable(false);
					GridBagConstraints gbc_txtVlcPath = new GridBagConstraints();
					gbc_txtVlcPath.gridwidth = 2;
					gbc_txtVlcPath.insets = new Insets(0, 0, 5, 5);
					gbc_txtVlcPath.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtVlcPath.gridx = 3;
					gbc_txtVlcPath.gridy = 1;
					panel.add(txtVlcPath, gbc_txtVlcPath);
					txtVlcPath.setColumns(10);
				}
				{
					btnClearVlcPath = new JButton("X");
					btnClearVlcPath.setEnabled(false);
					btnClearVlcPath.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setMediaFrameworkNativeDir(MediaFramework.VLC, null);
                        }
                    });
					
					
					GridBagConstraints gbc_btnClearVlcPath = new GridBagConstraints();
					gbc_btnClearVlcPath.insets = new Insets(0, 0, 5, 5);
					gbc_btnClearVlcPath.gridx = 5;
					gbc_btnClearVlcPath.gridy = 1;
					panel.add(btnClearVlcPath, gbc_btnClearVlcPath);
				}
				{
				    radioUseGst = new JRadioButton("GST");
				    buttonGroup.add(radioUseGst);
			        radioUseGst.addItemListener(new ItemListener() {
			            @Override
			            public void itemStateChanged(ItemEvent e) {
			                if (radioUseGst.isSelected()) {
			                    onFrameWorkSelected(MediaFramework.GST);
			                }
			            }
			        });
				    GridBagConstraints gbc_radioUseGst = new GridBagConstraints();
				    gbc_radioUseGst.anchor = GridBagConstraints.WEST;
				    gbc_radioUseGst.insets = new Insets(0, 0, 5, 5);
				    gbc_radioUseGst.gridx = 1;
				    gbc_radioUseGst.gridy = 2;
				    panel.add(radioUseGst, gbc_radioUseGst);
				}
				{
				    btnGstPath = new JButton("Gstreamer Path");
				    btnGstPath.addActionListener(new ActionListener() {
				        @Override
                        public void actionPerformed(ActionEvent e) {
				            onFrameworkPath(MediaFramework.GST);
				        }
				    });
				    btnGstPath.setEnabled(false);
				    GridBagConstraints gbc_btnGstPath = new GridBagConstraints();
				    gbc_btnGstPath.fill = GridBagConstraints.HORIZONTAL;
				    gbc_btnGstPath.insets = new Insets(0, 0, 5, 5);
				    gbc_btnGstPath.gridx = 2;
				    gbc_btnGstPath.gridy = 2;
				    panel.add(btnGstPath, gbc_btnGstPath);
				}
				{
                    File dir = Settings.getInstance().getMediaFrameworkNativeDir(MediaFramework.VLC);
                    txtGstPath = new JTextField(dir == null ? "" : dir.getAbsolutePath());
				    txtGstPath.setEditable(false);
				    txtGstPath.setColumns(10);
				    GridBagConstraints gbc_txtGstPath = new GridBagConstraints();
				    gbc_txtGstPath.gridwidth = 2;
				    gbc_txtGstPath.insets = new Insets(0, 0, 5, 5);
				    gbc_txtGstPath.fill = GridBagConstraints.HORIZONTAL;
				    gbc_txtGstPath.gridx = 3;
				    gbc_txtGstPath.gridy = 2;
				    panel.add(txtGstPath, gbc_txtGstPath);
				}
				{
				    btnClearGstPath = new JButton("X");
				    btnClearGstPath.addActionListener(new ActionListener() {
				        @Override
                        public void actionPerformed(ActionEvent e) {
				            setMediaFrameworkNativeDir(MediaFramework.GST, null);
				        }
				    });
				    btnClearGstPath.setEnabled(false);
				    GridBagConstraints gbc_btnClearGstPath = new GridBagConstraints();
				    gbc_btnClearGstPath.insets = new Insets(0, 0, 5, 5);
				    gbc_btnClearGstPath.gridx = 5;
				    gbc_btnClearGstPath.gridy = 2;
				    panel.add(btnClearGstPath, gbc_btnClearGstPath);
				}
				{
				    radioUseJgst = new JRadioButton("JST");
				    radioUseJgst.addItemListener(new ItemListener() {
				        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (radioUseJgst.isSelected()) {
                                onFrameWorkSelected(MediaFramework.JST);
                            }
				        }
				    });
				    buttonGroup.add(radioUseJgst);
				    GridBagConstraints gbc_radioUseJgst = new GridBagConstraints();
				    gbc_radioUseJgst.anchor = GridBagConstraints.WEST;
				    gbc_radioUseJgst.insets = new Insets(0, 0, 0, 5);
				    gbc_radioUseJgst.gridx = 1;
				    gbc_radioUseJgst.gridy = 3;
				    panel.add(radioUseJgst, gbc_radioUseJgst);
				}
			}
			{
				JScrollPane scrollPane = new JScrollPane();
				tabbedPane.addTab("Parameters", null, scrollPane, null);
				{
					gridPanel = new JPanel();
					scrollPane.setViewportView(gridPanel);
					gridPanel.setLayout(new GridBagLayout());
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				okButton.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
				{
					Component horizontalGlue = Box.createHorizontalGlue();
					buttonPane.add(horizontalGlue);
				}
				{
					JButton btnReset = new JButton("Reset");
					btnReset.addActionListener(new ActionListener() {
						@Override
                        public void actionPerformed(ActionEvent e) {
							resetParams();
						}
					});
					buttonPane.add(btnReset);
				}
				{
					Component horizontalGlue = Box.createHorizontalGlue();
					buttonPane.add(horizontalGlue);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				Component horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
		}
	}

	
	private void onFrameWorkSelected(MediaFramework mediaFramework) {
	    
	    Settings.getInstance().setMediaFramework(mediaFramework);
	    	    
	    radioUseVlc.setSelected(mediaFramework == MediaFramework.VLC);
        radioUseGst.setSelected(mediaFramework == MediaFramework.GST);
        radioUseJgst.setSelected(mediaFramework == MediaFramework.JST);
        
        btnClearGstPath.setEnabled(mediaFramework == MediaFramework.GST);
        btnClearVlcPath.setEnabled(mediaFramework == MediaFramework.VLC);
        
        btnGstPath.setEnabled(mediaFramework == MediaFramework.GST);
        btnVlcPath.setEnabled(mediaFramework == MediaFramework.VLC);
	}
	
	private void onFrameworkPath(MediaFramework mediaFramework) {
		
	    JFileChooser fc = new JFileChooser(Settings.getInstance().getMediaFrameworkNativeDir(mediaFramework)) {
		
			@Override
            public void approveSelection() {
				if (getSelectedFile().isDirectory()) {
					super.approveSelection();
				}
			}
		};
		
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		String _libname;
		
		final String soSuffix = RuntimeUtil.isNix() ? "so" : "dll";
		
		switch (mediaFramework) {
		    case VLC:
		        _libname = RuntimeUtil.getLibVlcName();
		        break;
		    case GST:
		        _libname = "libgstreamer-0.10." + soSuffix + ", gstreamer-0.10." + soSuffix;
		        break;
		        default:
		            throw new AssertionError("HDIGH!"); 
		}
		
		final String libname = _libname;
		
		fc.setFileFilter(new FileFilter() {
						
			@Override
			public String getDescription() {
				return "Directory containing " + libname;
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().matches(".*" + soSuffix + (RuntimeUtil.isNix() ? "(\\.[0-9\\.]+)?" : "") + "$");
			}
		});
		
		
		int status = fc.showOpenDialog(this);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			setMediaFrameworkNativeDir(mediaFramework, selectedFile);
		}
	}

    private void setMediaFrameworkNativeDir(ExternalMedia.MediaFramework mediaFramework, File selectedFile) {
        JTextField txt;
        
        switch (mediaFramework) {
            case GST:
                txt = txtGstPath;
                break;
            case VLC:
                txt = txtVlcPath;
                break;
                default:
                    logger.error("neither VLC nor GST framework {}", mediaFramework);
                    assert false: "HDIGH: neither VLC nor GST framework";
                    return;
                
        }
        
        Settings.getInstance().setMediaFrameworkNativeDir(mediaFramework, selectedFile);
        
        txt.setText(selectedFile == null ? "" : selectedFile.getAbsolutePath());
    }


	protected void resetParams() {
		for (Entry<String, Parameter> p: ps.getParamMap().entrySet()) {
			Parameter param = p.getValue();
			ps.setParam(param, param.getDefaultValue());
		}			
	}

	void init(RoboStroke rs) {
		
	    MediaFramework mediaFramework = Settings.getInstance().getMediaFramework();
	    
	    onFrameWorkSelected(mediaFramework);
	    
		ps = rs.getParameters();
		
		EventQueue.invokeLater(new Runnable() {
						
			@Override
			public void run() {
				
				int gridy = -1;
				
				for (Entry<String, Parameter> param: ps.getParamMap().entrySet()) {
					
					gridy++;
					
					final String key = param.getKey();
					final JTextField text = new JTextField(param.getValue().convertToString());
					
					{
						JLabel nameLabel = new JLabel(param.getValue().getName());
						GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
						gbc_lblNewLabel.insets = new Insets(0, 0, 0, 10);
						gbc_lblNewLabel.gridx = 0;
						gbc_lblNewLabel.gridy = gridy;
						gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
						gridPanel.add(nameLabel, gbc_lblNewLabel);
						
						nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						nameLabel.setForeground(Color.BLUE);
						
						nameLabel.addMouseListener(new MouseAdapter() {
							@Override
                            public void mouseClicked(MouseEvent e) {
								if (e.getButton() == MouseEvent.BUTTON1) {
									String url = "http://nargila.org/trac/robostroke/wiki/GuideParameters#" + key;
									try {
										java.awt.Desktop.getDesktop().browse(URI.create(url));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							}
						});
					}
					{
						JLabel lblDefault = new JLabel(param.getValue().getDefaultValue().toString());
						GridBagConstraints gbc_lblDefault = new GridBagConstraints();
						gbc_lblDefault.insets = new Insets(0, 0, 0, 5);
						gbc_lblDefault.gridx = 1;
						gbc_lblDefault.gridy = gridy;
						gbc_lblDefault.fill = GridBagConstraints.HORIZONTAL;
						gridPanel.add(lblDefault, gbc_lblDefault);
					}
					{
						GridBagConstraints gbc_lblValue = new GridBagConstraints();
						gbc_lblValue.gridx = 2;
						gbc_lblValue.gridy = gridy;
						gbc_lblValue.fill = GridBagConstraints.HORIZONTAL;
						gridPanel.add(text, gbc_lblValue);
					}

					paramEditorsMap.put(key, text);

					text.addFocusListener(new FocusListener() {

						@Override
						public void focusLost(FocusEvent e) {
							ps.setParam(key, text.getText());
						}

						@Override
						public void focusGained(FocusEvent e) {
						}
					});
				}

				ps.addListener("*", new ParameterChangeListener() {

					@Override
					public void onParameterChanged(final Parameter param) {
						EventQueue.invokeLater(new Runnable() {

							@Override
							public void run() {
								paramEditorsMap.get(param.getId()).setText(param.convertToString());
							}
						});
					}
				});				
			}
		});
	}
}
