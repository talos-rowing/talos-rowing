package org.nargila.robostroke.data.media;

import java.io.File;
import java.io.IOException;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.data.SynchedFileDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MediaSynchedFileDataInput extends SynchedFileDataInput {

    private static final Logger logger = LoggerFactory.getLogger(MediaSynchedFileDataInput.class);

    public static final String PROP_TIME_OFFSET = "timeOffset";

	public static final String PROP_TALOS_DATA = "talosData";

	public static final String PROP_VIDEO_EFFECT = "videoEffect";

	public static final String PROP_SYCH_MARK_ID = "synchMarkId";

    private final ExternalMedia media;

	public MediaSynchedFileDataInput(RoboStroke roboStroke, File dataFile, ExternalMedia _media, long synchTimeOffset, int synchMarkId) throws IOException {
		super(roboStroke, dataFile, synchTimeOffset, synchMarkId);
		
		this.media = _media;
		
		this.media.setEventListener(new ExternalMedia.EventListener() {
			
			@Override
			public void onEvent(ExternalMedia.EventType event) {
				switch (event) {
				case DURATION:
					setSeakable(media.getDuration() != 0);
					break;
				case PLAY:
				case PAUSE:
				case STOP:
					break;
				}
			}
		});
		
	}
	

	@Override
    public void stop() {
    	logger.info("stopping media..");
    	media.stop();    	
       	super.stop();
    }

    @Override
    public void start() {
    	logger.info("starting media..");
        super.start();
        media.start();
    }

    public void skipTime(long ms) {
    	double pos = (double)(media.getTime() + ms) / media.getDuration();
    	setPos(pos);
    }
    
    public void setRate(double rate) {
    	media.setRate(rate);
    }
    
    @Override
    public void skipReplayTime(float velocityX) {
    }

    @Override
    protected double calcProgress() throws IOException {    	
    	return media.getDuration() == 0 ? 0.0 : (double)media.getTime() / media.getDuration();
    }
    
    @Override
    public void setPaused(boolean pause) {

        if (pause) {
            media.pause();
        } else {
        	media.play();
        }
        
        super.setPaused(pause);

    }

    @Override
    protected void onSetPosPending(double pos) {
        setPaused(true);
    }

    @Override
    protected void onSetPosFinish(double pos) {
        if (media.getDuration() > 0) {

            long time = (long) (pos * media.getDuration());

            logger.info("seeking to timestamp {}ms", time);

            if (!media.setTime(time)) {
                logger.error("failed to seek to timestamp {}ms", time);
            }
            
            pos = time2pos(time);
            
            super.onSetPosFinish(pos);
            
            setPaused(false);
        }
    }


    @Override
    protected long getCurrentTime() {

    	return media.getTime();
    }
}