package org.nargila.robostroke.app;

import java.awt.Canvas;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFrame;

import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.input.RecordDataInput;
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
	
	private final JFrame videoFrame = new JFrame();
	private final Canvas videoCanvas = new Canvas();
	private final TalosPipeline jst = new TalosPipeline();
	private long duration;
	private boolean seekable;
	private boolean isBuffering;
	private int pipeState = Pipeline.NONE;
	
	OggDataInput(File f, RoboStrokeEventBus bus) {
		
		super(bus);
		
		videoCanvas.setSize(500, 400);
		videoFrame.getContentPane().add(videoCanvas);
		videoFrame.pack();
		videoFrame.setLocationRelativeTo(null);
		
		jst.setComponent(videoCanvas);
		jst.setTalosRecordPlayer(this);
		
		try {
			jst.setUrl(f.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException("HDIGH!", e);
		}
		
		jst.addPadListener(this);
			
		jst.getBus().addHandler(this);
	}

	@Override
	public void padRemoved(Pad pad) {					
	}
	
	@Override
	public void padAdded(Pad pad) {
	}
	
	@Override
	public void noMorePads() {	
		videoFrame.setVisible(jst.hasVideo());
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
                
                seekable = duration != -1;

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
		videoFrame.setVisible(false);
		jst.setState(Pipeline.NONE);
	}

	@Override
	public void start() {
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
		if (seekable) {
			jst.setState(Pipeline.PAUSE);
		}
	}

	@Override
	protected void onSetPosFinish(double pos) {
		if (seekable) {
			jst.setPos(pos);
			jst.setState(Pipeline.PLAY);			
		}
	}
}