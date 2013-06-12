/*
 * Copyright (c) 2013 Tal Shalif
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
import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.gstreamer.Buffer;
import org.gstreamer.Bus;
import org.gstreamer.BusSyncReply;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.Format;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Message;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.BaseSink;
import org.gstreamer.elements.FakeSink;
import org.gstreamer.event.BusSyncHandler;
import org.gstreamer.interfaces.XOverlay;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.common.ThreadedQueue;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.RecordDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

class GstExternalDataInput extends RecordDataInput implements Bus.DURATION, Bus.ERROR, Bus.WARNING, Bus.INFO, Bus.STATE_CHANGED, Element.PAD_ADDED, Element.NO_MORE_PADS {

    private static final Logger logger = LoggerFactory.getLogger(GstExternalDataInput.class);
    private final Canvas canvas = new Canvas();
    private static final String overlayFactory = Platform.isWindows() ? "directdrawsink" : "xvimagesink";
    private Element videoSink;

    static {
        Gst.init();
    }

    private Pipeline pipe;
    private Element dec;

    private long duration;
    private final Container container;


    boolean paused;

    private class KateQueue extends ThreadedQueue<Pair<Long,String>> {

    	long lastPos;

		KateQueue() {
			super("KateQueue", 100);
			setEnabled(true);
		}
		
		@Override
        final protected void handleItem(Pair<Long,String> o) {
			
		    playRecord(o.second);
			
		    if (duration > 0) {
		    	long ms = o.first;

		    	if (Math.abs(ms - lastPos) > 500) {

		    		double progress = ms / (double)duration;
		    		logger.info("BYTEPOSITION: progress={}", progress);
		    		bus.fireEvent(DataRecord.Type.REPLAY_PROGRESS, progress);
		    		lastPos = ms;
		    	}
		    }
		}
	}
	
    final KateQueue kateQueue = new KateQueue();
	
    private final BaseSink.HANDOFF kateHandsoff = new BaseSink.HANDOFF() {
        @Override
        public void handoff(BaseSink sink, Buffer buffer, Pad pad) {
            ByteBuffer buff = buffer.getByteBuffer();
            int len = buffer.getSize();
            byte[] data = new byte[len];
            buff.get(data);
            String text = new String(data);

            kateQueue.put(Pair.create(buffer.getTimestamp().toMillis(), text));

        }
    };
	private FakeSink kate;
	
	private XOverlay xoverlay;
	private final File videoFile;
	private final File srtFile;
    
    GstExternalDataInput(File videoFile, RoboStroke roboStroke, Container container) throws Exception {

        super(roboStroke);

        this.videoFile = videoFile;
        
        this.srtFile = new File(videoFile.getAbsolutePath().replaceFirst("\\.[a-zA-Z0-9]+$", ".srt"));

        if (!srtFile.exists()) {
            throw new IOException("file " + srtFile + " does not exist");
        }

        this.container = container;
    }


    private void setupPipeline(final File video, final File srtf) throws Exception {

    	String pipedesc = String.format("filesrc name=oggsrc location=%s ! decodebin2 name=dec  " +
    			"dec. ! ffmpegcolorspace ! %s name=videoSink force-aspect-ratio=true " +
    			"dec. ! audioconvert ! autoaudiosink " +
    			"filesrc name=katesrc location=%s ! subparse ! fakesink name=kate signal-handoffs=true  dump=false sync=true", video.getAbsolutePath().replace('\\', '/'), overlayFactory, srtf.getAbsolutePath().replace('\\', '/'));

    	pipe = Pipeline.launch(pipedesc);

    	videoSink = pipe.getElementByName("videoSink");

    	canvas.setBackground(Color.BLACK);

    	container.add(canvas, BorderLayout.CENTER);

    	logger.info("gst-launch {}", pipedesc);

    	pipe.getBus().connect((Bus.DURATION) this);
    	pipe.getBus().connect((Bus.INFO) this);
    	pipe.getBus().connect((Bus.WARNING) this);
    	pipe.getBus().connect((Bus.ERROR) this);
    	pipe.getBus().connect((Bus.STATE_CHANGED) this);

    	if (!Platform.isWindows()) {
    		pipe.getBus().setSyncHandler(new BusSyncHandler() {

    			public BusSyncReply syncMessage(Message msg) {
    				Structure s = msg.getStructure();
    				if (s == null || !s.hasName("prepare-xwindow-id")) {
    					return BusSyncReply.PASS;
    				}
    				xoverlay = XOverlay.wrap(videoSink);
    				xoverlay.setWindowHandle(canvas);
    				return BusSyncReply.DROP;
    			}
    		});
    	} else {

    		xoverlay = XOverlay.wrap(videoSink);

    		xoverlay.setWindowHandle(canvas);
    	}

    	kate = (FakeSink) pipe.getElementByName("kate");

    	dec = pipe.getElementByName("dec");

    	dec.connect((Element.PAD_ADDED) this);
    	dec.connect((Element.NO_MORE_PADS) this);

    	kate.connect(kateHandsoff);
    }


    public void durationChanged(GstObject source, Format format, long duration) {
        logger.info("duration: {}", duration);
        if (source == pipe) {
            this.duration = duration;
        }
    }

    public void noMorePads(Element element) {
        if (duration == 0) {
            duration = pipe.queryDuration(TimeUnit.MILLISECONDS);
            logger.info("duration: {}", duration);
        }

        setSeakable(duration > 0);
    }

    public void padAdded(Element element, Pad pad) {

        Caps caps = pad.getCaps();

        logger.info("new pad with caps {}", caps);


    }


    @Override
    public void stop() {
    	logger.info("stopping pipeline..");
    	pipe.stop();
        
    	container.remove(canvas);
    	
       	super.stop();
    }


    @Override
    public void start() {

        super.start();


        try {
			setupPipeline(videoFile, srtFile);
		} catch (Exception e) {
			logger.error("failed to setup pipeline", e);
			return;
		}

        pipe.play();
    }

    @Override
    public void skipReplayTime(float velocityX) {
    }

    @Override
    public void setPaused(boolean pause) {

        this.paused = pause;

        if (pause) {
            pipe.pause();
        } else {
            pipe.play();
        }

    }

    @Override
    protected void onSetPosPending(double pos) {
        setPaused(true);
    }

    @Override
    protected void onSetPosFinish(double pos) {
        if (duration > 0) {
            setPaused(false);

            long time = (long) (pos * duration);

            logger.info("seeking to timestamp {}ms", time);

            if (!pipe.seek(time, TimeUnit.MILLISECONDS)) {
                logger.error("failed to seek to timestamp {}ms", time);
            }
        }
    }

    @Override
    public boolean isSeekable() {
        return true;
    }

    @Override
    public void infoMessage(GstObject source, int code, String message) {
        logger.info("{}: {}", source.getName(), message);

    }

    @Override
    public void warningMessage(GstObject source, int code, String message) {
        logger.warn("{}: {}", source.getName(), message);
    }

    @Override
    public void errorMessage(GstObject source, int code, String message) {
        logger.error("{}: {}", source.getName(), message);
    }

    @Override
    public void stateChanged(GstObject source, State old, State current, State pending) {

        if (source == pipe) {
            switch (current) {
                case PAUSED:
                    if (duration == 0) {
                        duration = pipe.queryDuration(TimeUnit.MILLISECONDS);
                        logger.info("duration: {}", duration);

                        setSeakable(duration > 0);
                    }
                    break;
                    default:
                    	break;
            }
        }
    }
}