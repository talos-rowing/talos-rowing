package org.nargila.robostroke.media.vlc;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class VlcFindQrMarkPipeline {

	private static final Logger logger = LoggerFactory.getLogger(VlcFindQrMarkPipeline.class);
	
	private final AtomicReference<Exception> finishSync = new AtomicReference<Exception>();
	
	private String mark;

	private ClockTime timestamp;

	private BufferedImage currentImage;
	
	private final EmbeddedMediaPlayerComponent vlc = new EmbeddedMediaPlayerComponent() {
		
		private void logme() {
			boolean dummy = true;
		}
		
	    @Override
	    public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl) {
	    }

	    @Override
	    public void opening(MediaPlayer mediaPlayer) {
	    }

	    @Override
	    public void buffering(MediaPlayer mediaPlayer, float newCache) {
	    }

	    @Override
	    public void playing(MediaPlayer mediaPlayer) {
	    }

	    @Override
	    public void paused(MediaPlayer mediaPlayer) {
	    	logme();
	    }

	    @Override
	    public void stopped(MediaPlayer mediaPlayer) {
	    	logme();
	    }

	    @Override
	    public void forward(MediaPlayer mediaPlayer) {
	    	logme();
	    }

	    @Override
	    public void backward(MediaPlayer mediaPlayer) {
	    }

	    @Override
	    public void finished(MediaPlayer mediaPlayer) {
	    	logme();
	    }

	    @Override
	    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
	    	mp.pause();	   
	    	mp.getSnapshot();
	    	logme();
	    }

	    @Override
	    public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
	    	logme();
	    }

	    @Override
	    public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {
	    	logme();
	    }

	    @Override
	    public void pausableChanged(MediaPlayer mediaPlayer, int newSeekable) {
	    }

	    @Override
	    public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {
	    }

	    @Override
	    public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
	    	
			{
		    	File f = new File(filename);
		    	
				try {				
					FileOutputStream output = new FileOutputStream("/tmp/bula.png");
					FileInputStream in = new FileInputStream(f);
					
					output.getChannel().transferFrom(in.getChannel(), 0, f.length());
					output.close();
					in.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
	    	try {
				BufferedImage image = ImageIO.read(new File(filename));
								
				findQrCode(image, mp.getTime());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    	if (mark != null) {
	    		stop();
	    	} else {
	    		Runnable run = new Runnable() {
					
					@Override
	    			public void run() {
	    				mp.nextFrame();
	    				try {
	    					Thread.sleep(200);
	    					mp.getSnapshot();
	    				} catch (InterruptedException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}							
	    			}
				};
	    		new Thread(run, "sleep 200") {

	    		}.start();
	    	}

	    	logme();
	    }

	    @Override
	    public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
	    }

	    @Override
	    public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
	    }

	    @Override
	    public void error(MediaPlayer mediaPlayer) {
	    }

	    @Override
	    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
	    }

	    @Override
	    public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {
	    }

	    @Override
	    public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
	    }

	    @Override
	    public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
	    }

	    @Override
	    public void mediaFreed(MediaPlayer mediaPlayer) {
	    }

	    @Override
	    public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
	    }

	    @Override
	    public void newMedia(MediaPlayer mediaPlayer) {
	    }

	    @Override
	    public void subItemPlayed(MediaPlayer mediaPlayer, int subItemIndex) {
	    }

	    @Override
	    public void subItemFinished(MediaPlayer mediaPlayer, int subItemIndex) {
	    }

	    @Override
	    public void endOfSubItems(MediaPlayer mediaPlayer) {
	    }
	};

	private File video;

	private EmbeddedMediaPlayer mp;

	protected BufferedImage bi;
	
	public VlcFindQrMarkPipeline(File video) {		
		this.video = video;
		mp = vlc.getMediaPlayer();
	}
	
	void start() {
		mp.playMedia(video.getAbsolutePath());
	}
	
	void stop() {
		mp.stop();
		vlc.release();

		synchronized (finishSync) {
			finishSync.notifyAll();
		}
	}
	

	private boolean findQrCode(BufferedImage image, long time) {
		
        LuminanceSource source =  new BufferedImageLuminanceSource(image);
                
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        ClockTime t = ClockTime.fromMillis(time);
        
        try {
            
        	Result result = new MultiFormatReader().decode(bitmap);
            mark = result.getText();
            
            timestamp = t;
            
            logger.info("mark: {}, timestamp: {}, pipetime: {}", new Object[]{ mark, timestamp.toMillis(), vlc.getMediaPlayer().getTime() });
            
        } catch (NotFoundException e) {
            
            logger.info("timestamp: {}", t);
            
        	return false;
        }
        
        return true;
	}
	
	
	public Pair<Integer,Long> findMark(int timeoutSeconds) throws Exception {
				
		synchronized (finishSync) {
			start();
			finishSync.wait(timeoutSeconds * 1000);
		}
		
		stop();
		
		if (mark == null) {
			
			if (finishSync.get() != null) {
				throw finishSync.get();
			}
			
			throw new IllegalStateException("could not find QR sync mark in video within " + timeoutSeconds + " seconds");
		}
		
		return Pair.create(new Integer(mark.split(":")[1]),timestamp.toMillis());
	}

	public static void main(String[] args) throws Exception {
		
		final VlcFindQrMarkPipeline qrFind = new VlcFindQrMarkPipeline(new File(args[0]));
		
	    JFrame f = new JFrame("Test Player");
//	    f.setIconImage(new ImageIcon(MinimalTestPlayer.class.getResource("/icons/vlcj-logo.png")).getImage());
	    f.setSize(800, 600);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    f.addWindowListener(new WindowAdapter() {
	      @Override
	      public void windowClosing(WindowEvent e) {
	    	  qrFind.vlc.release();
	      }
	    });
	    
	    JPanel p = new JPanel(new BorderLayout());
	    Box buttons = new Box(BoxLayout.X_AXIS);
	    JButton pause = new JButton("=|>");
	    JButton next = new JButton(">>");
	    JButton snap = new JButton("Snap");
	    final JLabel time = new JLabel();
	    time.setText("00:00:00,000");
	    time.setFont(Font.getFont(Font.MONOSPACED));
	    
	    pause.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				qrFind.mp.pause();
			}
		});
	    
	    next.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				qrFind.mp.nextFrame();
				time.setText(org.nargila.robostroke.common.ClockTime.fromMillis(qrFind.mp.getTime()).toString());
			}
		});
	    
	    snap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				qrFind.bi = qrFind.mp.getSnapshot();
			}
		});
	    
	    buttons.add(pause);
	    buttons.add(next);
	    buttons.add(snap);
	    buttons.add(time);
	    
	    p.add(buttons, BorderLayout.SOUTH);
	    p.add(qrFind.vlc, BorderLayout.CENTER);
	    
	    f.setContentPane(p);
	    	    
	    f.setVisible(true);
	    
		qrFind.findMark(160);
	}
	
    private BufferedImage getBufferedImage(int width, int height) {
    	
        if (currentImage != null && currentImage.getWidth() == width
                && currentImage.getHeight() == height) {
            return currentImage;
        }
        if (currentImage != null) {
            currentImage.flush();
        }
        
        currentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        currentImage.setAccelerationPriority(0.0f);
        return currentImage;
    }   
}
