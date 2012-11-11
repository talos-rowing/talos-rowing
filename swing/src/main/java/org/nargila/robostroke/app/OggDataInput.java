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

package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.io.File;
import java.net.MalformedURLException;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.RecordDataInput;
import org.nargila.robostroke.jst.TalosPipeline;

import com.fluendo.jst.BusHandler;
import com.fluendo.jst.Element;
import com.fluendo.jst.Message;
import com.fluendo.jst.Object;
import com.fluendo.jst.Pad;
import com.fluendo.jst.PadListener;
import com.fluendo.jst.Pipeline;
import com.fluendo.utils.Debug;

class OggDataInput extends RecordDataInput implements BusHandler, PadListener {
	
	private final Canvas videoCanvas = new Canvas();
	private final TalosPipeline jst = new TalosPipeline();
	private long duration;
	private boolean isBuffering;
	private int pipeState = Pipeline.NONE;
	private final Container container;
	
	OggDataInput(File f, RoboStroke roboStroke, Container container) {
		
		super(roboStroke);
		
		this.container = container;
		container.add(videoCanvas, BorderLayout.CENTER);
		jst.setComponent(videoCanvas);
		jst.setTalosRecordPlayer(this);
		jst.setKeepAspect(true);
		
		try {
			jst.setUrl(f.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException("HDIGH!", e);
		}
		
		jst.addPadListener(this);
			
		jst.getBus().addHandler(this);
		
		videoCanvas.addComponentListener(new ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				jst.resizeVideo();
			}
		});
	}

	@Override
	public void padRemoved(Pad pad) {					
	}
	
	@Override
	public void padAdded(Pad pad) {
	}
	
	@Override
	public void noMorePads() {	
		container.setVisible(jst.hasVideo());
	}

	@Override
    public void handleMessage(Message msg) {
		
		Object src = msg.getSrc();

        switch (msg.getType()) {
            case Message.WARNING:
                Debug.info(msg.toString());
                break;
            case Message.ERROR:
                Debug.info(msg.toString());
                break;
            case Message.EOS:
                Debug.log(Debug.INFO, "EOS: playback ended (src=" + src + ")");
                setPaused(true);
                break;
            case Message.STREAM_STATUS:
                Debug.info(msg.toString());
                break;
            case Message.RESOURCE:
                Debug.info(msg.parseResourceString());
                break;
            case Message.DURATION:

            	duration = msg.parseDurationValue();

                Debug.log(Debug.INFO, "got duration: " + duration + " (src=" + src + ")");
                
                setSeakable(duration != -1);

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
                            break;
                        case Element.PLAY:
                            break;
                        case Element.STOP:
                            break;
                    }
                    
                    pipeState = next;
                }
                break;
            case Message.BYTEPOSITION:
            	long position = msg.parseBytePosition();
            	if (pipeState == Pipeline.PLAY && duration != -1) {
            		double progress = position / (double)duration;
                    Debug.log(Debug.INFO, "BYTEPOSITION: progress=" + progress + " (src=" + src + ")");
					bus.fireEvent(DataRecord.Type.REPLAY_PROGRESS, progress);
            	}
                break;
            default:
                break;
        }
    }

	@Override
	public void stop() {
		container.remove(videoCanvas);
		container.setVisible(false);
		jst.setState(Pipeline.NONE);
		
		super.stop();
	}

	@Override
	public void start() {
		
		super.start();
		
		jst.setState(Pipeline.PLAY);
	}

	@Override
	public void skipReplayTime(float velocityX) {
	}

	@Override
	public void setPaused(boolean pause) {
		jst.setState(pause ? Pipeline.PAUSE : Pipeline.PLAY);
	}

	@Override
	protected void onSetPosPending(double pos) {
		jst.setState(Pipeline.PAUSE);
	}

	@Override
	protected void onSetPosFinish(double pos) {
		jst.setPos(pos);
		jst.setState(Pipeline.PLAY);			
	}
}