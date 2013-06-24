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
import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.gstreamer.Bin;
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

import com.sun.jna.Native;
import com.sun.jna.Platform;

class GstDataInput extends RecordDataInput implements Bus.DURATION, Bus.ERROR, Bus.WARNING, Bus.INFO, Bus.STATE_CHANGED, Element.PAD_ADDED, Element.NO_MORE_PADS {
	
	private static final Logger logger = LoggerFactory.getLogger(GstDataInput.class);
    private final Canvas canvas = new Canvas();
    private static final String overlayFactory = Platform.isWindows() ? "directdrawsink" : "xvimagesink";
    private final Element videoSink;

    private class KateQueue extends ThreadedQueue<Buffer> {

    	long lastPos;

		KateQueue() {
			super("KateQueue", 100);
			setEnabled(true);
		}
		
		@Override
        final protected void handleItem(Buffer buffer) {
            ByteBuffer buff = buffer.getByteBuffer();

            if (!paused && buff.get() == 0) {
                long start = buff.getLong();
                @SuppressWarnings("unused")
				long _duration = buff.getLong();
                @SuppressWarnings("unused")
				long backlink = buff.getLong();
                int len = buff.getInt();

                byte[] data = new byte[len];

                buff.get(data);

                String text = new String(data);

                handle(Pair.create(start, text));

            }
        }
        
		private void handle(Pair<Long, String> o) {
			
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
	
	
	static {
		Gst.init();
	}
	
	private Pipeline pipe;
    private Element dec;

    private final Caps CAPS_KATE1, CAPS_KATE2, CAPS_THEORA, CAPS_VORBIS;

	private long duration;
	private final Container container;
	private final Bin videoBin, audioBin, kateBin;

	private final KateQueue kateQueue = new KateQueue();
	
	boolean paused;
	
	GstDataInput(File f, RoboStroke roboStroke, Container container) {

		super(roboStroke);

        CAPS_KATE1 = new Caps("application/x-kate");
        CAPS_KATE2 = new Caps("subtitle/x-kate");
        CAPS_THEORA = new Caps("video/x-theora");
        CAPS_VORBIS = new Caps("audio/x-vorbis");

		this.container = container;

		String demuxName;
		
		if (f.getName().toLowerCase().endsWith(".ogg")) {
			demuxName = "oggdemux";
		} else if (f.getName().toLowerCase().endsWith(".mkv")) {
			demuxName = "matroskademux";
		} else {
			throw new IllegalArgumentException("can't figure out demuxer to use for input file " + f);
		}
		
		pipe = Pipeline.launch(String.format("filesrc name=src location=%s ! %s name=dec", f.getAbsolutePath().replace('\\', '/'), demuxName));

        videoBin = createBin(String.format("queue ! theoradec name=src ! ffmpegcolorspace ! %s name=videoSink force-aspect-ratio=true", overlayFactory), "videoBin");

        videoSink = videoBin.getElementByName("videoSink");

        canvas.setBackground(Color.RED);

        container.add(canvas, BorderLayout.CENTER);

        audioBin = createBin("queue ! vorbisdec ! audioconvert ! autoaudiosink", "audioBin");
        kateBin = createBin("queue ! kateparse ! fakesink name=kate signal-handoffs=true  dump=false", "kateBin");


        if (true) {
            pipe.getBus().connect((Bus.DURATION)this);
            pipe.getBus().connect((Bus.INFO)this);
            pipe.getBus().connect((Bus.WARNING)this);
            pipe.getBus().connect((Bus.ERROR)this);
            pipe.getBus().connect((Bus.STATE_CHANGED)this);

            if (!Platform.isWindows()) {
            	pipe.getBus().setSyncHandler(new BusSyncHandler() {

            		public BusSyncReply syncMessage(Message msg) {
            			Structure s = msg.getStructure();
            			if (s == null || !s.hasName("prepare-xwindow-id")) {
            				return BusSyncReply.PASS;
            			}
            			XOverlay.wrap(videoSink).setWindowHandle(Native.getComponentID(canvas));
            			return BusSyncReply.DROP;
            		}
            	});
            } else {
            	XOverlay.wrap(videoSink).setWindowHandle(Native.getComponentID(canvas));
            }

            FakeSink kate = (FakeSink) kateBin.getElementByName("kate");

            dec = pipe.getElementByName("dec");

            dec.connect((Element.PAD_ADDED)this);
            dec.connect((Element.NO_MORE_PADS)this);

		kate.connect(new BaseSink.HANDOFF() {
			
			
			@Override
			public void handoff(BaseSink sink, Buffer buffer, Pad pad) {
                kateQueue.put(buffer);
            }
		});
        }
    }


    private static Bin createBin(String spec, String name) {
        Bin bin = Bin.launch(spec, true);
        bin.setName(name);
        return bin;
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
        pipe.play();
    }

    public void padAdded(Element element, Pad pad) {

        Caps caps = pad.getCaps();

            logger.info("new pad with caps {}", caps);

        if (caps.isSubset(CAPS_THEORA)) {
            addVideo(pad);
        } else if (caps.isSubset(CAPS_VORBIS)) {
            addAudio(pad);
        } else if (caps.isSubset(CAPS_KATE1)) {
            addKate(pad);
        } else if (caps.isSubset(CAPS_KATE2)) {
            addKate(pad);
        } else {
            logger.warn("ignoring pad with unknown caps {}", caps);
        }
    }

    private void addVideo(Pad pad) {
        linkBin(pad, videoBin);
    }
    private void addAudio(Pad pad) {
        linkBin(pad, audioBin);
    }
    private void addKate(Pad pad) {
        linkBin(pad, kateBin);
    }

    private void linkBin(Pad pad, Bin bin) {

        logger.info("linking {} to {}", pad.getCaps(), bin.getName());
        pipe.add(bin);
        pad.link(bin.getSinkPads().iterator().next());
        bin.play();
    }
    @Override
	public void stop() {
		container.removeAll();
		container.setVisible(false);
		pipe.stop();
		
		super.stop();
	}

	@Override
	public void start() {
		
		super.start();
		
		
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

			long time = (long)(pos * duration);
			
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
			}
		}
	}
}