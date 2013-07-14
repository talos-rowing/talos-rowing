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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.io.File;
import java.net.MalformedURLException;

import org.nargila.robostroke.data.media.ExternalMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluendo.jst.BusHandler;
import com.fluendo.jst.Element;
import com.fluendo.jst.Format;
import com.fluendo.jst.Message;
import com.fluendo.jst.Pad;
import com.fluendo.jst.PadListener;
import com.fluendo.jst.Pipeline;
import com.fluendo.jst.Query;
import com.fluendo.utils.Debug;

public class JstExternalMedia implements ExternalMedia, BusHandler, PadListener {
	
    private static final Logger logger = LoggerFactory.getLogger(JstExternalMedia.class);
    
	private final Canvas videoCanvas = new Canvas();
	private final TalosPipeline jst = new TalosPipeline();
	private long duration;
	private boolean isBuffering;
	private int pipeState = Pipeline.NONE;
	private final Container container;
	private final Listeners listeners = new Listeners();
    
	private boolean stepMode = false;
		
    private TimeNotifiyer timeNotifier;

    private final File videoFile;
    
	public JstExternalMedia(File videoFile, Container container, VideoEffect videoEffect) {
				
		this.container = container;
		this.videoFile = videoFile;
		
		container.add(videoCanvas, BorderLayout.CENTER);
		jst.setComponent(videoCanvas);
		jst.setKeepAspect(true);
		
		try {
			jst.setUrl(videoFile.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException("HDIGH!", e);
		}
		
		jst.addPadListener(this);
			
		jst.getBus().addHandler(this);
		
		videoCanvas.addComponentListener(new ComponentAdapter() {
			@Override
            public void componentResized(java.awt.event.ComponentEvent e) {
				jst.resizeVideo();
			}
		});
		
		timeNotifier = new TimeNotifiyer(listeners) {
            @Override
            protected long getTime() {                
                return jst.getTime() / 1000;
            }		    
		};
	}

	@Override
	public void padRemoved(Pad pad) {					
	}
	
	@Override
	public void padAdded(Pad pad) {
	}
	
	@Override
	public void noMorePads() {	
	    	    
	    new Thread() {
	        @Override
	        public void run() {

	            com.fluendo.jst.Event event;

	            /* get value, convert to PERCENT and construct seek event */
	            event = com.fluendo.jst.Event.newSeek(Format.BYTES, videoFile.length());
	            /* send event to pipeline */
	            boolean res = jst.sendEvent(event);
	            res = jst.setPos(0.99);
	            
	            Query pq = Query.newPosition(Format.PERCENT);
	            
	            res = jst.query(pq);

	            long bp = pq.parsePositionValue();

	            pq = Query.newDuration(Format.TIME);

	            res = jst.query(pq);

	            long l = pq.parsePositionValue();
	                
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	            jst.setState(Pipeline.PLAY);
	            long t = jst.getTime() / 1000;
	            
	            Query q = Query.newDuration(Format.TIME);
	            
	            if (jst.query(q)) {
	                long d = q.parseDurationValue();
	                logger.info("query duration == {}", d);
	            }
	        }
	    }.start();
        
		container.setVisible(jst.hasVideo());
	}

	@Override
    public void handleMessage(Message msg) {
		
		Object src = msg.getSrc();

        switch (msg.getType()) {
            case Message.WARNING:
                logger.warn(msg.toString());
                break;
            case Message.ERROR:
                logger.error(msg.toString());
                break;
            case Message.EOS:
                Debug.log(Debug.INFO, "EOS: playback ended (src=" + src + ")");
                pause();
                break;
            case Message.STREAM_STATUS:
                Debug.info(msg.toString());
                break;
            case Message.RESOURCE:
                Debug.info(msg.parseResourceString());
                break;
            case Message.DURATION:

            	duration = msg.parseDurationValue() / 1000;

            	long d = 0;
            	
            	logger.info("DURATION message: {}, getDuration(): {}", duration, d);
            	
                Debug.log(Debug.INFO, "got duration: " + duration + " (src=" + src + ")");
                
                listeners.dispatch(EventType.DURATION, duration);
                
                break;
            case Message.BUFFERING:
                boolean busy;
                int percent;

                busy = msg.parseBufferingBusy();
                percent = msg.parseBufferingPercent();

                if (busy) {
                    if (!isBuffering) {
                        Debug.log(Debug.INFO, "PAUSE: we are buffering");
                        isBuffering = true;
                    }
                } else {
                    if (isBuffering) {
                        Debug.log(Debug.INFO, "PLAY: we finished buffering");
                        isBuffering = false;
                    }
                    Debug.log(Debug.INFO, "BUFFERING: percent=" + percent);
                }
                break;
            case Message.STATE_CHANGED:
            	if (src == jst) {
                    int old, next;

                    old = msg.parseStateChangedOld();
                    next = msg.parseStateChangedNext();
                    
                    Debug.log(Debug.INFO, "STATE_CHANGED: old=" + old + ", next=" + next);

                    switch (next) {
                        case Element.PAUSE:
                            
                            listeners.dispatch(EventType.TIME, getTime());
                            
                            if (!stepMode) {
                                listeners.dispatch(EventType.PAUSE, true);                               
                            }
                            break;
                        case Element.PLAY:
                            if (stepMode) {
                                jst.setState(Pipeline.PAUSE);
                            } else {
                                listeners.dispatch(EventType.PLAY, true);
                            }
                            break;
                        case Element.STOP:
                            listeners.dispatch(EventType.STOP, true);
                            break;
                    }
                    
                    pipeState = next;
                }
                break;
            case Message.BYTEPOSITION:
            	long position = msg.parseBytePosition();
            	if (pipeState == Pipeline.PLAY) {
            		double progress = position / (double)videoFile.length();
                    Debug.log(Debug.INFO, "BYTEPOSITION: progress=" + progress + " (src=" + src + ")");
            	}
                break;
            default:
                break;
        }
    }

	@Override
	public void stop() {
	    timeNotifier.stop();
	    jst.setState(Pipeline.NONE);		
		container.remove(videoCanvas);
	}

	@Override
	public void start() {	
		jst.setState(Pipeline.PLAY);
		timeNotifier.start();
	}

	@Override
	public void pause() {
		jst.setState(Pipeline.PAUSE);
	}

    @Override
    public void play() {
        setStepMode(false);
        jst.setState(Pipeline.PLAY);
    }

    @Override
    public void addEventListener(EventListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        listeners.removeListener(listener);
    }
    
    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public long getTime() {
        return jst.getTime() / 1000;
    }

    @Override
    public boolean setTime(long time) {
        
        double pos = (double)time / duration;
        
        logger.info("seeking to file pos {} ({} / {})", new Object[]{pos, time, duration});
        
//        return jst.setTime(time * 1000);
        return jst.setPos(pos);
    }

    @Override
    public boolean isPlaying() {
        return pipeState == Element.PLAY;
    }

    private void setStepMode(boolean stepMode) {
        if (this.stepMode != stepMode) {
            
            jst.setStepMode(stepMode);
            this.stepMode = stepMode;

            if (stepMode) {
                jst.setState(Pipeline.PAUSE);
                listeners.dispatch(EventType.PAUSE, true); 
            }

            
        }
    }
    
    @Override
    public boolean step() {
        
        setStepMode(true);
        
        jst.setState(Pipeline.PLAY);

        return true;
    }

    @Override
    public boolean setRate(double rate) {
        return false;
    }
}