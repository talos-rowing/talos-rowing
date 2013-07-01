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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterService;

import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class ParamEditDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private ParameterService ps;

	private final HashMap<String, JTextField> paramEditorsMap = new HashMap<String, JTextField>();
	private JPanel gridPanel;
	private JTextField txtVlcPath;
	
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
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					JButton btnVlcPath = new JButton("VLC Path");
					btnVlcPath.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onVlcPath();
						}
					});
					GridBagConstraints gbc_btnVlcPath = new GridBagConstraints();
					gbc_btnVlcPath.insets = new Insets(0, 0, 0, 5);
					gbc_btnVlcPath.gridx = 1;
					gbc_btnVlcPath.gridy = 1;
					panel.add(btnVlcPath, gbc_btnVlcPath);
				}
				{
					txtVlcPath = new JTextField(Settings.getInstance().getVlcLibDir() == null ? "" : Settings.getInstance().getVlcLibDir().getAbsolutePath());
					txtVlcPath.setEditable(false);
					GridBagConstraints gbc_txtVlcPath = new GridBagConstraints();
					gbc_txtVlcPath.gridwidth = 2;
					gbc_txtVlcPath.insets = new Insets(0, 0, 0, 5);
					gbc_txtVlcPath.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtVlcPath.gridx = 2;
					gbc_txtVlcPath.gridy = 1;
					panel.add(txtVlcPath, gbc_txtVlcPath);
					txtVlcPath.setColumns(10);
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

	
	@SuppressWarnings("serial")
	protected void onVlcPath() {
		JFileChooser fc = new JFileChooser(Settings.getInstance().getVlcLibDir()) {    
			public void approveSelection() {
				if (getSelectedFile().isDirectory()) {
					super.approveSelection();
				}
			}
		};
		
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		fc.setFileFilter(new FileFilter() {
						
			@Override
			public String getDescription() {
				return "Directory containing " + RuntimeUtil.getLibVlcName();
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().matches("^" + RuntimeUtil.getLibVlcName() + (RuntimeUtil.isNix() ? "(\\.[0-9\\.]+)?" : "") + "$");
			}
		});
		
		
		int status = fc.showOpenDialog(this);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			Settings.getInstance().setVlcLibDir(selectedFile);
			txtVlcPath.setText(selectedFile.getAbsolutePath());
		}
	}

	protected void resetParams() {
		for (Entry<String, Parameter> p: ps.getParamMap().entrySet()) {
			Parameter param = p.getValue();
			ps.setParam(param, param.getDefaultValue());
		}			
	}

	void init(RoboStroke rs) {
		
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
	public JTextField getTxtVlcPath() {
		return txtVlcPath;
	}
}
