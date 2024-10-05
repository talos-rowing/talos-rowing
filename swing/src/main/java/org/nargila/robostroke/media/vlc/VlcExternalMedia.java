package org.nargila.robostroke.media.vlc;

import org.nargila.robostroke.data.ClockProvider;
import org.nargila.robostroke.data.SystemClockProvider;
import org.nargila.robostroke.data.media.ExternalMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.awt.*;
import java.io.File;

public class VlcExternalMedia implements ExternalMedia {

    private static final Logger logger = LoggerFactory.getLogger(VlcExternalMedia.class);

    private final VlcEmbeddedPlayer playerComponent;

    private long duration;

    private final Container container;

    private final File videoFile;

    private final VideoEffect videoEffect;

    private final Listeners listeners = new Listeners();

    private double rate = 1.0;

    VlcExternalMedia(File videoFile, Container container) throws Exception {
        this(videoFile, container, VideoEffect.NONE);
    }

    public VlcExternalMedia(File videoFile, Container container, VideoEffect videoEffect) throws Exception {

        VlcSetup.setupCheckVlc(container);

        this.videoFile = videoFile;
        this.container = container;
        this.videoEffect = videoEffect;

        playerComponent = new VlcEmbeddedPlayer();

        container.add(playerComponent, BorderLayout.CENTER);

    }


    private void setDuration(long duration) {

        if (this.duration == 0 && duration != 0) {
            this.duration = duration;
            logger.info("duration: {}", duration);
            listeners.dispatch(EventType.DURATION, duration);
        }
    }


    @Override
    public void stop() {
        logger.info("stopping pipeline..");
        playerComponent.stop();
        playerComponent.release();
        container.remove(playerComponent);
    }


    @Override
    public void start() {
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

        private final ClockProvider clock = new ClockProvider() {

            private final ClockProvider deadReconingClock = new SystemClockProvider() {
                {
                    run();
                }
            };

            private long lastPlayTime;

            @Override
            public void stop() {
                // nothing to do
            }

            @Override
            public void run() {
                // nothing to do
            }

            @Override
            public void reset(long initialTime) {
                // nothing to do

            }

            @Override
            public long getTime() {

                long playTime = mediaPlayer.getTime();

                if (!playing || playTime != lastPlayTime || rate < 0.5) {
                    lastPlayTime = playTime;
                    deadReconingClock.reset(0);
                    return playTime;
                }

                long deadReconingTimeElapsed = deadReconingClock.getTime();
                if (playing && deadReconingTimeElapsed < 300) {
                    logger.info("dead reconing timestamp {} (diff={})", lastPlayTime + deadReconingTimeElapsed, deadReconingTimeElapsed);
                    return lastPlayTime + deadReconingTimeElapsed;
                }

                return playTime;
            }
        };

        private final MediaPlayer mediaPlayer;
        private boolean stopped;
        private boolean playing;

        private VlcEmbeddedPlayer() {
            mediaPlayer = getMediaPlayer();
        }

        public void setTime(long time) {
            mediaPlayer.setTime(time);
        }

        public void setPause(boolean paused) {
            playing = false;
            mediaPlayer.setPause(paused);
        }

        private long getTime() {
            return clock.getTime();
        }

        private synchronized void stop() {
            if (!stopped) {
                mediaPlayer.stop();
            }

            stopped = true;

        }

        private synchronized void start(File file) {

            String transformation;

            switch (videoEffect) {
                case ROTATE180:
                    transformation = "180";
                    break;
                case ROTATE270:
                    transformation = "270";
                    break;
                case ROTATE90:
                    transformation = "90";
                    break;
                default:
                    transformation = null;
                    break;
            }


            String[] args = transformation == null ? new String[0] : new String[]{"--video-filter=transform", "--transform-type=" + transformation};

            mediaPlayer.setStandardMediaOptions(args);
            mediaPlayer.startMedia(file.getAbsolutePath());
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            playing = true;
            listeners.dispatch(EventType.PLAY, true);
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
            listeners.dispatch(EventType.TIME, newTime);
        }

        @Override
        public void paused(MediaPlayer mediaPlayer) {
            listeners.dispatch(EventType.TIME, getTime());
            playing = false;
            listeners.dispatch(EventType.PAUSE, true);
        }


        @Override
        public void stopped(MediaPlayer mediaPlayer) {
            stopped = true;
            playing = false;
            listeners.dispatch(EventType.STOP, true);
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

    @Override
    public boolean setRate(double rate) {
        if (playerComponent.mediaPlayer.setRate((float) rate) == 0) {
            this.rate = rate;
            return true;
        }

        return false;
    }

    @Override
    public boolean step() {
        playerComponent.mediaPlayer.nextFrame();
        listeners.dispatch(EventType.TIME, getTime());
        return true;
    }
}
