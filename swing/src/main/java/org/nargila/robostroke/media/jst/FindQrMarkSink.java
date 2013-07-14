/*
 * Copyright (c) 2012 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */



package org.nargila.robostroke.media.jst;

import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Caps;
import com.fluendo.jst.Pad;
import com.fluendo.jst.Sink;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class FindQrMarkSink extends Sink {
	
	String mark;
	long timestamp;
	final Object finishSync = "";
	
	private static final Logger logger = LoggerFactory.getLogger(FindQrMarkSink.class);
	
	private int width;
	private int height;

	public FindQrMarkSink() {
		setName("findqrsink");
				
	}


	@Override
    protected boolean setCapsFunc (Caps caps) {
		String mime = caps.getMime();
		if (!mime.equals ("video/raw"))
			return false;

		width = caps.getFieldInt("width", -1);
		height = caps.getFieldInt("height", -1);

		if (width == -1 || height == -1)
			return false;

		return true;
	}

	@Override
    protected int preroll (Buffer buf) {
		return render (buf);
	}


	@Override	
	protected int render (Buffer buf) {	  
		
	    if (!buf.duplicate) {

    		logger.info("recieved image at offset:{}, timestamp: {}", buf.offset, buf.timestamp);
    		
	    	if (buf.object instanceof ImageProducer) {
	    		logger.info("buf.object instanceof ImageProducer");
	    		int[] pixels = new int[width * height];
	    		
	    		ImageProducer imageProducer = (ImageProducer)buf.object;

	    		PixelGrabber pg = new PixelGrabber(imageProducer, 0, 0, width, height, pixels, 0, width);

	    		try {
	    			pg.grabPixels();
	    		} catch (InterruptedException e1) {
		    		return Pad.ERROR;
	    		}

	    		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	    		bi.setRGB(0, 0, width, height, pixels, 0, width);
	    		
	    		if (findQrCode(bi, buf)) {
	    			
	    			synchronized (finishSync) {
	    				finishSync.notifyAll();
	    			}
	    		}

	    	} else {
	    		logger.error(this+": unknown buffer received "+buf);
	    		return Pad.ERROR;
	    	}
	    }

		return Pad.OK;
	}

	private boolean findQrCode(BufferedImage image, Buffer buf) {
		
        LuminanceSource source =  new BufferedImageLuminanceSource(image);
        
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            
        	Result result = new MultiFormatReader().decode(bitmap);
            mark = result.getText();
            timestamp = buf.timestamp;
            
            logger.info("mark: {}, timestamp: {}", mark, timestamp);
            
        } catch (NotFoundException e) {
            
        	return false;
        }
        
        return true;
	}


	@Override	
	public String getFactoryName () 	{
		return "findqrsink";
	}
}
