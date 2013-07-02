package org.nargila.robostroke.media.vlc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.gstreamer.ClockTime;
import org.nargila.robostroke.app.Settings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class SteppingPlayerDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	protected ClockTime markTime;
	private VlcFrameSteppingPlayerPanel player;
	private String mrl;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SteppingPlayerDialog dialog = new SteppingPlayerDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.launch(args[0]);
			
			if (dialog.getTime() != null) {
				System.out.println(dialog.getTime());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SteppingPlayerDialog() {
		
		VlcSetup.setupCheckVlc(this);
		

		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Find Sync Mark");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			player = new VlcFrameSteppingPlayerPanel();
			player.setBorder(new LineBorder(Color.GRAY, 5));
			contentPanel.add(player);
			
			player.setTimeListener(new VlcFrameSteppingPlayerPanel.TimeChangeListener() {
				
				@Override
				public void onTimeChanged(long time) {
					markTime = ClockTime.fromMillis(time);
				}
			});
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						markTime = null;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public ClockTime getTime() {
		return markTime;
	}
	
	@Override
    protected void processWindowEvent(WindowEvent e) {
        
		super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_OPENED) {
        	player.play(mrl);
        }
    }
	
	@Override
	public void dispose() {
		player.stop();
		super.dispose();
	}
	
	public void launch(String mrl) {
		this.mrl = mrl;
		setVisible(true);
	}
}
