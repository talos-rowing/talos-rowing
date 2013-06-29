package org.nargila.robostroke.media.gst;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.gstreamer.Buffer;
import org.gstreamer.Bus;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Fraction;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.Structure;
import org.gstreamer.elements.BaseSink;
import org.gstreamer.elements.FakeSink;
import org.gstreamer.elements.FileSrc;
import org.gstreamer.elements.RGBDataSink;
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

	private BufferedImage currentImage;

	static {
		Gst.init();
	}
	
	public GstFindQrMarkPipeline(File video) {
		
		pipe = Pipeline.launch("filesrc name=src ! decodebin2 name=dec ! ffmpegcolorspace name=end");
		
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
    	
    	RGBSink sink = new RGBSink();
    	
    	pipe.add(sink);
    	
    	pipe.getElementByName("end").link(sink);
    	
	}
	
	void start() {
		pipe.play();
	}
	
	public void stop() {
		pipe.stop();
		
		synchronized (finishSync) {
			finishSync.notifyAll();
		}
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
    
    private BaseSink sinkElement;
    
    private class RGBSink extends RGBDataSink {


		public RGBSink() {
			super("rgb", new RGBDataSink.Listener() {
				public synchronized void rgbFrame(boolean isPrerollFrame, int width, int height, IntBuffer rgb) {

					if (mark == null) {
						try {
							final BufferedImage renderImage = getBufferedImage(width, height);
							int[] pixels = ((DataBufferInt) renderImage.getRaster().getDataBuffer()).getData();
							rgb.get(pixels, 0, width * height);  

							if (findQrCode(renderImage, sinkElement.getLastBuffer())) {
								synchronized (finishSync) {
									finishSync.notifyAll();
								}
							}
						} catch (Exception e) {
							logger.error("image conversion or QR code detection error", e);
							synchronized (finishSync) {
								finishSync.set(e);
								finishSync.notifyAll();
							}
						}                
					} 
				}
			});
			
			sinkElement = getSinkElement();
			sinkElement.enableLastBuffer(true);
			sinkElement.setSync(false);
		}
    }
}
