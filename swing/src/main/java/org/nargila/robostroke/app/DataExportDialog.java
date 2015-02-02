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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.FileDataInput;
import org.nargila.robostroke.data.RecordDataInput;

@SuppressWarnings("serial")
public class DataExportDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPanel panel;

	private final HashMap<DataRecord.Type, JCheckBox> exportTypeMap = new HashMap<DataRecord.Type, JCheckBox>();
	private RoboStroke rs;
	private JButton exportButton;
	protected boolean cancelled;
	private final AtomicReference<DataExporter> exporter = new AtomicReference<DataExporter>();
	private int selectedCount;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DataExportDialog dialog = new DataExportDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DataExportDialog(RoboStroke rs) {
		
		this.rs = rs;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportBorder(new EmptyBorder(10, 10, 10, 10));
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				panel = new JPanel();
				scrollPane.setViewportView(panel);
				panel.setLayout(new GridBagLayout());
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				exportButton = new JButton("Export");
				exportButton.setEnabled(false);
				exportButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							exportData();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
				exportButton.setActionCommand("OK");
				buttonPane.add(exportButton);
				getRootPane().setDefaultButton(exportButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						synchronized (exporter) {
							if (exporter.get() != null) {
								exporter.get().cancel();
							}
						}
						
						cancelled = true;
						
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		addTypes();
	}

	private void exportData() throws IOException {

		exportButton.setEnabled(false);
		
		FileDataInput input = (FileDataInput) rs.getDataInput();
		
		final File dataFile = input.getDataFile();
		final HashSet<DataRecord.Type> exportSet = new HashSet<DataRecord.Type>();
		
		for (Entry<DataRecord.Type, JCheckBox> e: exportTypeMap.entrySet()) {
			if (e.getValue().isSelected()) {
				exportSet.add(e.getKey());
			}
		}
		
		synchronized (exporter) {
			exporter.set(new DataExporter(dataFile, exportSet) {
				protected void onFinish() {
					setVisible(false);
				}
			});
		
			new Thread("DataExportDialog exporter") {
				public void run() {				

					try {
						exporter.get().export();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	@Override
	public void setVisible(boolean b) {
		
		if (b) {
			RecordDataInput input = (RecordDataInput) rs.getDataInput();
			input.setPaused(true);
		}
		
		super.setVisible(b);
	}
	
	private void addTypes() {
		
		int count = 0;
		for (DataRecord.Type type: DataRecord.Type.values()) {
			if (type.isExportableEvent) {
				final JCheckBox cb = new JCheckBox(type.name());
				exportTypeMap.put(type, cb);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = count % 3;
				gbc.gridy = count / 3;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				count++;
				
				cb.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						updateSelected(cb.isSelected() ? 1 : -1);
					}
				});
				panel.add(cb, gbc);
			}
		}
	}

	private void updateSelected(int i) {
		
		selectedCount += i;
		
		exportButton.setEnabled(selectedCount > 0);
	}
}
