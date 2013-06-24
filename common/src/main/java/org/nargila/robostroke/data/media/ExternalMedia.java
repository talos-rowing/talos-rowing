package org.nargila.robostroke.data.media;

public interface ExternalMedia {
	
	public enum EventType {
		PLAY,
		PAUSE,
		STOP,
		DURATION
	}
	
	public interface EventListener {
		public void onEvent(ExternalMedia.EventType event);
	}
	
	public void setEventListener(EventListener listener);
	
	public long getDuration();
	public long getTime();
	public boolean setTime(long time);
	public boolean isPlaying();
	public void start();
	public void play();
	public void pause();
	public void stop();	
}
