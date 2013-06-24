package org.nargila.robostroke.oggz;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.gstreamer.Buffer;
import org.gstreamer.Bus;
import org.gstreamer.ClockTime;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.BaseSink;
import org.gstreamer.elements.FakeSink;
import org.gstreamer.elements.FileSrc;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class GstFindQrMarkPipeline {

	private static final Logger logger = LoggerFactory.getLogger(GstFindQrMarkPipeline.class);
	
	private final AtomicReference<Exception> finishSync = new AtomicReference<Exception>();
	
	private Pipeline pipe;

	private String mark;

	private ClockTime timestamp;

	public GstFindQrMarkPipeline(File video) {
		
		pipe = Pipeline.launch("filesrc name=src ! decodebin2 name=dec ! ffmpegcolorspace ! pngenc snapshot=false ! fakesink name=sink signal-handoffs=true");
		
		FileSrc src = (FileSrc) pipe.getElementByName("src");
		
		src.setLocation(video);
		
    	pipe.getBus().connect(new Bus.INFO() {

			@Override
			public void infoMessage(GstObject source, int code, String message) {
				logger.info(message);				
			}
    		
    	});
    	
    	pipe.getBus().connect(new Bus.WARNING() {

			@Override
			public void warningMessage(GstObject source, int code,
					String message) {
				
				logger.warn(message);
				
			}
    		
    	});
    	
    	pipe.getBus().connect(new Bus.ERROR() {

			@Override
			public void errorMessage(GstObject source, int code, String message) {
				logger.error(message);
			}    		
    	});
    	
    	FakeSink sink = (FakeSink) pipe.getElementByName("sink");
    	
    	sink.connect(new BaseSink.HANDOFF() {
            @Override
            public void handoff(BaseSink sink, Buffer buffer, Pad pad) {
                
            	ByteBuffer buff = buffer.getByteBuffer();
                int len = buffer.getSize();
                byte[] data = new byte[len];
                buff.get(data);
                
                try {
					
                	BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
					
					if (findQrCode(img, buffer)) {
						synchronized (finishSync) {
							finishSync.notifyAll();
						}
					}
				} catch (IOException e) {
					logger.error("failed to convert buffer to BufferedImage", e);
					synchronized (finishSync) {
						finishSync.set(e);
						finishSync.notifyAll();
					}
				}                

            }
        });
	}
	
	private void start() {
		pipe.play();
	}
	
	private void stop() {
		pipe.stop();
	}
	

	private boolean findQrCode(BufferedImage image, Buffer buf) {
		
        LuminanceSource source =  new BufferedImageLuminanceSource(image);
        
        ClockTime _timestamp = buf.getTimestamp();
        
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            
        	Result result = new MultiFormatReader().decode(bitmap);
            mark = result.getText();
            
            timestamp = _timestamp;
            
            logger.info("mark: {}, timestamp: {}, pipetime: {}", new Object[]{ mark, timestamp.toMillis(), pipe.queryPosition().toMillis() });
            
        } catch (NotFoundException e) {
            
            logger.info("timestamp: {}", _timestamp);
            
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
		
		Gst.init();
		
		GstFindQrMarkPipeline qrFind = new GstFindQrMarkPipeline(new File(args[0]));
		
		qrFind.findMark(160);
	}
}
