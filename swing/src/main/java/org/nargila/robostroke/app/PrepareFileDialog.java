package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class PrepareFileDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	protected boolean cancelled;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PrepareFileDialog dialog = new PrepareFileDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public PrepareFileDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Prepare File");
		setBounds(100, 100, 450, 119);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			Component verticalGlue = Box.createVerticalGlue();
			contentPanel.add(verticalGlue);
		}
		{
			progressBar = new JProgressBar();
			contentPanel.add(progressBar);
		}
		{
			Component verticalGlue = Box.createVerticalGlue();
			contentPanel.add(verticalGlue);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			{
				Component horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
			{
				Component horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
		}
	}
	
	void launch(File trsd) {
		
		File f = uncimpressFile(trsd);
		
		onFinish(f);
		
	}
	
	
	void cancel() {
		cancelled = true;
	}
	
	private File uncimpressFile(File trsd) {
		
		byte[] buff = new byte[4096];
		InputStream is = null;
		OutputStream os = null;
		
		try {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			
			final long size = trsd.length();

			File res = File.createTempFile("talos-rowing-data", ".txt");
			res.deleteOnExit();



			is = new GZIPInputStream(new FileInputStream(trsd));

			os = new FileOutputStream(res);


			long accum = 0;

			for (int i =  is.read(buff); !cancelled && i != -1; i =  is.read(buff)) {

				Thread.yield();
				
				os.write(buff, 0, i);

				accum += i;

				int pos = (int) (100.0 * (accum / (double) size));

				progressBar.setValue(pos);
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			if (!cancelled) {
				return res;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) try {is.close();} catch (Exception e) {}
			if (os != null) try {os.close();} catch (Exception e) {}
		}
		
		return null;
	}
	
	protected void onFinish(File f) {
		
	}

}
