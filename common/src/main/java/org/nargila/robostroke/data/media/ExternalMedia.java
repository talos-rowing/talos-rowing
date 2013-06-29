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
	
	public enum VideoEffect {
		NONE("none"),
		ROTATE90("clockwise"),
		ROTATE180("rotate-180"),
		ROTATE270("counterclockwise");
		
		
		public final String method;
	
		VideoEffect(String method) {
			this.method = method;
		}
		
		@Override
		public String toString() {
			return method;
		}
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
