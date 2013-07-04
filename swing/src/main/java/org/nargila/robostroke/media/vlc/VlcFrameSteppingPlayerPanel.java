package org.nargila.robostroke.media.vlc;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nargila.robostroke.common.ClockTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.player.MediaPlayer;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;

@SuppressWarnings("serial")
public class VlcFrameSteppingPlayerPanel extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(VlcFrameSteppingPlayerPanel.class);

	public interface TimeChangeListener {
		public void onTimeChanged(String mark, ClockTime time);
	}
	
	private JButton btnPlay;
	private JButton btnNext;
	private TimeChangeListener timeListener;
	private JSlider slider;
	private BufferedImagePlayerComponent vlc;
	private JButton btnSkipBack;
	private JButton btnSkipForeward;
	private JLabel lblTime;
	private JCheckBox chckbxAutoQR;
	protected boolean qrSearchMode = true;
	private String mark;
	private ClockTime timestamp;
	private JLabel lblMark;
	private Component glue;
	private Component glue_1;
	private Component glue_2;

	/**
	 * Create the panel.
	 */
	public VlcFrameSteppingPlayerPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		slider = new JSlider();
		slider.setValue(0);
		slider.setMaximum(1000);
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if (slider.getValueIsAdjusting()) {
					vlc.getMediaPlayer().setPosition((float)slider.getValue() / slider.getMaximum());
				}
			}
		});
		
		panel.add(slider);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		btnPlay = new JButton(">|=");
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vlc.getMediaPlayer().pause();
			}
		});
		panel_1.add(btnPlay);
		
		btnSkipBack = new JButton("-3");
		btnSkipBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vlc.getMediaPlayer().skip(-3000);
				updateTime();
			}
		});
		panel_1.add(btnSkipBack);
		
		btnSkipForeward = new JButton("+3");
		btnSkipForeward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vlc.getMediaPlayer().skip(3000);
				updateTime();
			}
		});
		panel_1.add(btnSkipForeward);
		
		btnNext = new JButton(">>");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vlc.getMediaPlayer().nextFrame();
				updateTime();
			}
		});
		panel_1.add(btnNext);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		glue = Box.createGlue();
		panel_3.add(glue);
		
		lblMark = new JLabel("1");
		lblMark.setHorizontalAlignment(SwingConstants.LEFT);
		panel_3.add(lblMark);
		
		glue_1 = Box.createGlue();
		panel_3.add(glue_1);
		
		lblTime = new JLabel("00:00:00,000");
		panel_3.add(lblTime);
		
		glue_2 = Box.createGlue();
		panel_3.add(glue_2);
		
		chckbxAutoQR = new JCheckBox("Auto QR");
		chckbxAutoQR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				qrSearchMode  = chckbxAutoQR.isSelected();
			}
		});
		chckbxAutoQR.setSelected(true);
		panel_1.add(chckbxAutoQR);
		
		vlc = new BufferedImagePlayerComponent() {
		    @Override
		    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
		    	updateTime(newTime);
		    }

		    
		    @Override
		    protected void onImageChanged(BufferedImage image, final long timestamp) {
		    	super.onImageChanged(image, timestamp);
		    	
		    	if (qrSearchMode) {
		    		
		    		if (findQrCode(image, timestamp)) {		    	
		    			vlc.getMediaPlayer().setPause(true);
		    			updateTime(timestamp);
		    		} 
		    	}
		    }
		    
		    @Override
		    public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {		    	
		    	slider.setValue((int) (newPosition * slider.getMaximum()));
		    	super.positionChanged(mediaPlayer, newPosition);
		    }
		};
		
		add(vlc.getCanvas(), BorderLayout.CENTER);
		
	}

	public void setTimeListener(TimeChangeListener listener) {
		this.timeListener = listener;		
	}
	
	private void updateTime() {
		updateTime(vlc.getMediaPlayer().getTime());
	}

	private void updateTime(final long time) {

		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				timeListener.onTimeChanged(mark, ClockTime.fromMillis(time));
				lblTime.setText(ClockTime.fromMillis(time).toString());
//				sslider.setValue((int) (vlc.getMediaPlayer().getPosition() * slider.getMaximum()));
				lblMark.setText(mark == null ? "S:1" : mark);
			}
		};
		
		EventQueue.invokeLater(run);
	}

	private boolean findQrCode(BufferedImage image, long time) {
		
        LuminanceSource source =  new BufferedImageLuminanceSource(image);
                
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        ClockTime t = ClockTime.fromMillis(time);
        
        try {
            
        	Result result = new MultiFormatReader().decode(bitmap);
            
        	mark = result.getText();
                        
            logger.info("mark: {}, timestamp: {}, pipetime: {}", new Object[]{ mark, t, vlc.getMediaPlayer().getTime() });
            
        } catch (NotFoundException e) {
            
            logger.info("timestamp: {}", t);
            
        	return false;
        }
        
        return true;
	}
	
	
	public static void main(String[] args) {
		
		final VlcFrameSteppingPlayerPanel player = new VlcFrameSteppingPlayerPanel();
		
	    JFrame f = new JFrame("Test Player");
//	    f.setIconImage(new ImageIcon(MinimalTestPlayer.class.getResource("/icons/vlcj-logo.png")).getImage());
	    f.setSize(800, 600);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    f.addWindowListener(new WindowAdapter() {
	      @Override
	      public void windowClosing(WindowEvent e) {
	    	  player.vlc.release();
	      }
	    });
	    
	    JPanel p = new JPanel(new BorderLayout());
	    
	    final JLabel lblTime = new JLabel();
	    lblTime.setText("00:00:00,000");
	    lblTime.setFont(Font.getFont(Font.MONOSPACED));

	    p.add(lblTime, BorderLayout.SOUTH);
	    
	    p.add(player, BorderLayout.CENTER);
	    
	    f.setContentPane(p);
	    
	    f.setVisible(true);
	    
	    player.setTimeListener(new TimeChangeListener() {
			
			@Override
			public void onTimeChanged(String mark, ClockTime time) {
				updateTime(lblTime, time);
			}

			private void updateTime(final JLabel lblTime, ClockTime t) {
				lblTime.setText(t.toString());
			}
		});
	    
	    player.play(args[0]);
	}
	
	public void play(String mrl) {
		vlc.getMediaPlayer().playMedia(mrl);		
	}

	public void stop() {
		vlc.getMediaPlayer().stop();
		vlc.release();
	}
	public JLabel getLblMark() {
		return lblMark;
	}
}
