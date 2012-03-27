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
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterService;

public class ParamEditDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private ParameterService ps;

	private final HashMap<String, JTextField> paramEditorsMap = new HashMap<String, JTextField>();
	private JPanel gridPanel;
	
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
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				gridPanel = new JPanel();
				scrollPane.setViewportView(gridPanel);
				gridPanel.setLayout(new GridBagLayout());
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

	protected void resetParams() {
		for (Entry<String, Parameter<?>> p: ps.getParamMap().entrySet()) {
			Parameter<?> param = p.getValue();
			ps.setParam(param, param.getDefaultValue());
		}			
	}

	void init(RoboStroke rs) {
		
		ps = rs.getParameters();
		
		EventQueue.invokeLater(new Runnable() {
						
			@Override
			public void run() {
				
				int gridy = -1;
				
				for (Entry<String, Parameter<?>> param: ps.getParamMap().entrySet()) {
					
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
					public void onParameterChanged(final Parameter<?> param) {
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
