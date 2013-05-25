package org.nargila.robostroke.oggz;

import javax.swing.JDialog;

public class CovertVideoDialog extends JDialog {

	public CovertVideoDialog() {

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setTitle("Video Convert");
		
		setContentPane(new VlcOggConvertPanel() {
			
			@Override
			protected void onClose() {
				setVisible(false);
				dispose();
			}
		});		
	}
}
