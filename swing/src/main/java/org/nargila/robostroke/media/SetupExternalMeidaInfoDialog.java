package org.nargila.robostroke.media;

import javax.swing.JDialog;

@SuppressWarnings("serial")
public class SetupExternalMeidaInfoDialog extends JDialog {

    public SetupExternalMeidaInfoDialog() {

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setTitle("Talos Video Merge");
		
        setContentPane(new SetupExternalMediaInfoPanel() {
            
            @Override
            protected void onClose() {
                setVisible(false);
                dispose();
            }
        });		
	}

}
