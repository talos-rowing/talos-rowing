package org.nargila.robostroke.media.vlc;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.nargila.robostroke.app.Settings;
import org.nargila.robostroke.data.media.ExternalMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class VlcExternalMedia implements ExternalMedia {

	private static final Logger logger = LoggerFactory.getLogger(VlcExternalMedia.class);
    
	private final VlcEmbeddedPlayer playerComponent;
    
    private long duration;

    private final Container container;

	private final File videoFile;
	
	private final VideoEffect videoEffect;

	private EventListener listener;
	
	VlcExternalMedia(File videoFile, Container container) throws Exception {
		this(videoFile, container, VideoEffect.NONE);
	}
	
	public VlcExternalMedia(File videoFile, Container container, VideoEffect videoEffect) throws Exception {
		
		VlcSetup.setupCheckVlc(container);
		
        this.videoFile = videoFile;
        this.container = container;        
        this.videoEffect = videoEffect;
        
        playerComponent = new VlcEmbeddedPlayer();
    }



    private void setDuration(long duration) {
    	
    	if (this.duration == 0 && duration != 0) {
    		this.duration = duration;
			logger.info("duration: {}", duration);
    		listener.onEvent(EventType.DURATION);
    	}
    }
    

    @Override
    public void stop() {
    	logger.info("stopping pipeline..");
    	playerComponent.stop();
    	container.remove(playerComponent);
    }


    @Override
    public void start() {
    	container.add(playerComponent, BorderLayout.CENTER);
    	playerComponent.start(videoFile);
    }

    @Override
    public void pause() {
    	playerComponent.setPause(true);
    }
    
    @Override
    public void play() {
    	playerComponent.mediaPlayer.play();
    }

	@Override
	public void setEventListener(EventListener listener) {
		this.listener = listener;
	}

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public long getTime() {
		return playerComponent.getTime();
	}

	@Override
	public boolean setTime(long time) {
		
		playerComponent.setTime(time);
		
		return true;
	}

	@Override
	public boolean isPlaying() {
		return playerComponent.mediaPlayer.isPlaying();
	}
	
	
	
	@SuppressWarnings("serial")
	private class VlcEmbeddedPlayer extends EmbeddedMediaPlayerComponent {
		
		private final Thread playTimeSmoother = new Thread("playTimeSmoother") {
			
			private long lastTime;
			private long lastSystemTime;
			
			public void run() {
				while (!stopped) {
					if (playing) {
						synchronized (time) { 
							long t = time.get();
							if (lastTime != t || t == 0 || lastTime - t > 200) {
								lastSystemTime = System.currentTimeMillis();
								lastTime = time.get();
							} else {
								long currentTime = System.currentTimeMillis();
								lastTime += currentTime - lastSystemTime;
								time.set(lastTime);
								lastSystemTime = currentTime;
							}
						}
					}
					
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		private final MediaPlayer mediaPlayer;
		private boolean stopped;
		private boolean playing;

		private final AtomicLong time = new AtomicLong();
		
		private VlcEmbeddedPlayer() {
			mediaPlayer = getMediaPlayer();
		}
		
		public void setTime(long time) {
			synchronized (this.time) {
				this.time.set(time - 1);
				mediaPlayer.setTime(time);
			}
			
		}

		public void setPause(boolean paused) {
			playing = false;
			mediaPlayer.setPause(paused);
		}

		private long getTime() {
			return time.get();
		}
		
		private synchronized void stop() {
			if (!stopped) {
				mediaPlayer.stop();
			}
			
			stopped = true;
			
			try {
				playTimeSmoother.interrupt();
				playTimeSmoother.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private synchronized void start(File file) {
			mediaPlayer.playMedia(file.getAbsolutePath());
			playTimeSmoother.start();
		}
		
	    @Override
	    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
	    	synchronized (time) {
				time.set(newTime);
			}
	    }
	    
		@Override
	    public void playing(MediaPlayer mediaPlayer) {
			playing = true;
	    	listener.onEvent(EventType.PLAY);
	    }

	    @Override
	    public void paused(MediaPlayer mediaPlayer) {
	    	playing = false;
	    	listener.onEvent(EventType.PAUSE);
	    }

	    
	    @Override
	    public void stopped(MediaPlayer mediaPlayer) {
	    	stopped = true;
	    	playing = false;
	    	listener.onEvent(EventType.STOP);
	    }

	    @Override
	    public void error(MediaPlayer mediaPlayer) {
	    	logger.error("error");
	    }

	    @Override
	    public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
	    	setDuration(newDuration);
	    }
	}
}
