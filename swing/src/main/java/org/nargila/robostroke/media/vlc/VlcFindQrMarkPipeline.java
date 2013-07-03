package org.nargila.robostroke.media.vlc;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.sun.jna.Memory;

public class VlcFindQrMarkPipeline {

	private static final Logger logger = LoggerFactory.getLogger(VlcFindQrMarkPipeline.class);
	
	private final AtomicReference<Exception> finishSync = new AtomicReference<Exception>();
	
	private String mark;

	private ClockTime timestamp;
	
    private BufferedImage bi = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(1, 1);
    private int[] rgbBuffer = new int[bi.getWidth() * bi.getHeight()];
    
    private final AtomicBoolean playerDone = new AtomicBoolean();
    
	private final DirectMediaPlayerComponent vlc = new DirectMediaPlayerComponent(new BufferFormatCallback() {
				
				@Override
				public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
					return new RV32BufferFormat(sourceWidth, sourceHeight);
				}
			}) {
			   
	    @Override
	    public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
	    		    	
				synchronized (playerDone) {
					
					if (playerDone.get()) {
						return;
					}
					
					int width = bufferFormat.getWidth();
					int height = bufferFormat.getHeight();
					if (width != bi.getWidth() || height != bi.getHeight()) {
						bi = GraphicsEnvironment.getLocalGraphicsEnvironment()
								.getDefaultScreenDevice()
								.getDefaultConfiguration()
								.createCompatibleImage(width, height);
						rgbBuffer = new int[bi.getWidth() * bi.getHeight()];
					}
					nativeBuffers[0]
							.getByteBuffer(0L, nativeBuffers[0].size())
							.asIntBuffer()
							.get(rgbBuffer,
									0,
									bufferFormat.getHeight()
											* bufferFormat.getWidth());
					bi.setRGB(0, 0, bi.getWidth(), bi.getHeight(), rgbBuffer,
							0, bi.getWidth());
					if (findQrCode(bi, mediaPlayer.getTime())) {

						synchronized (finishSync) {
							finishSync.notifyAll();
						}
					}
				}
	    }
	};

	private File video;

	private MediaPlayer mp;
	
	public VlcFindQrMarkPipeline(File video) {		
		this.video = video;
		mp = vlc.getMediaPlayer();
	}
	
	void start() {
		mp.playMedia(video.getAbsolutePath());
	}
	
	
	public void stop() {
		
		synchronized (finishSync) {
			finishSync.notifyAll();
		}
	}

	private void doStop() {
		synchronized (playerDone) {
			
			if (playerDone.get()) {
				return;
			}
			
			mp.stop();
			vlc.release();
			
			
			playerDone.set(true);
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
		
		doStop();
		
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
	    
		qrFind.findMark(160);
	}
}
