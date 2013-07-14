package org.nargila.robostroke.media;

import javax.swing.JDialog;

import org.nargila.robostroke.app.Settings;

public class SetupExternalMeidaInfoDialog extends JDialog {

	private final SetupExternalMediaInfoPanel setupPanel;

    public SetupExternalMeidaInfoDialog() {

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setTitle("Talos Video Merge");
		
		setupPanel = new SetupExternalMediaInfoPanel() {
			
			@Override
			protected void onClose() {
				setVisible(false);
				dispose();
			}
		};
		
        setContentPane(setupPanel);		
	}

    public void loadSettings(Settings dataPrefs, long time) {
        setupPanel.loadSettings(dataPrefs, time);
    }
    
    public boolean isCanceled() {
        return setupPanel.canceled;
    }
}
