package org.nargila.robostroke.oggz;

import javax.swing.JDialog;

public class MergeTalosOggDialog extends JDialog {

	public MergeTalosOggDialog() {

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setTitle("Talos Video Merge");
		
		setContentPane(new MergeTalosOggPanel() {
			
			@Override
			protected void onClose() {
				setVisible(false);
				dispose();
			}
		});		
	}
}
