package org.nargila.robostroke.media.vlc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

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
	    
	private final DirectMediaPlayerComponent vlc = new BufferedImageMediaPlayer() {
		protected void onImageChanged(BufferedImage image, long timestamp)  {
			
			if (findQrCode(image, timestamp)) {

				synchronized (finishSync) {
					finishSync.notifyAll();
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
		mp.stop();
		vlc.release();
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
