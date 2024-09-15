package org.nargila.robostroke.data.media;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.nargila.robostroke.common.ListenerList;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ExternalMedia {

    public abstract class TimeNotifiyer {

        private static final Logger logger = LoggerFactory.getLogger(TimeNotifiyer.class);

        private final AtomicLong lastTime = new AtomicLong();

        private final ScheduledExecutorService timeExecutor = Executors.newSingleThreadScheduledExecutor();

        private final AtomicBoolean stopped = new AtomicBoolean();

        private final Runnable timeNotifyRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (stopped) {
                    if (!stopped.get()) {
                        long time = getTime();
                        lastTime.set(time);
                        listeners.dispatch(EventType.TIME, time);
                    }
                }
            }
        };

        private ScheduledFuture<?> timeNotifyJob;

        private final Listeners listeners;

        public TimeNotifiyer(Listeners listeners) {
            this.listeners = listeners;
        }

        public synchronized void start() {

            logger.info("starting time notifyer");

            if (timeNotifyJob != null) {
                throw new IllegalStateException("already started");
            }

            timeNotifyJob = timeExecutor.scheduleWithFixedDelay(timeNotifyRunnable, 0, 50, TimeUnit.MILLISECONDS);
        }

        public synchronized void stop() {

            logger.info("stopping time notifyer");

            synchronized (stopped) {
                stopped.set(true);
                if (timeNotifyJob != null) timeNotifyJob.cancel(true);
                timeExecutor.shutdownNow();
            }
        }

        public long getLastTime() {
            return lastTime.get();
        }

        protected abstract long getTime();
    }

    public class Listeners extends ListenerList<EventListener,Pair<EventType, Object>> {
        @Override
        protected void dispatch(EventListener listener, Pair<EventType, Object> eventObject) {
            listener.onEvent(eventObject.first, eventObject.second);
        }

        public void dispatch(EventType event, Object data) {
            dispatch(Pair.create(event, data));
        }
    }

    public enum MediaFramework {
        VLC,
        GST
    }

    public enum EventType {
        PLAY,
        PAUSE,
        STOP,
        DURATION,
        TIME
    }

    public interface EventListener {
        public void onEvent(ExternalMedia.EventType event, Object data);
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

    public void addEventListener(EventListener listener);
    public void removeEventListener(EventListener listener);

    public long getDuration();
    public long getTime();
    public boolean setTime(long time);
    public boolean isPlaying();
    public void start();
    public void play();
    public void pause();
    public void stop();
    public boolean step();
    public boolean setRate(double rate);

}
