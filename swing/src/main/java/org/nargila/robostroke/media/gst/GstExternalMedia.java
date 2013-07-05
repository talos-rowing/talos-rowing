package org.nargila.robostroke.media.gst;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.gstreamer.Bus;
import org.gstreamer.BusSyncReply;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.Format;
import org.gstreamer.GstObject;
import org.gstreamer.Message;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.SeekFlags;
import org.gstreamer.SeekType;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.event.BusSyncHandler;
import org.gstreamer.interfaces.XOverlay;
import org.nargila.robostroke.data.media.ExternalMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class GstExternalMedia implements ExternalMedia, Bus.ERROR, Bus.WARNING, Bus.INFO, Bus.STATE_CHANGED, Element.PAD_ADDED, Element.NO_MORE_PADS {

    private static final Logger logger = LoggerFactory.getLogger(GstExternalMedia.class);

    private final Canvas canvas = new Canvas();

    private static final String overlayFactory = Platform.isWindows() ? "directdrawsink" : "xvimagesink";

    private Element videoSink;

    @SuppressWarnings("unused")
    private final GstInitializer gstInitializer = GstInitializer.getInstance();

    private Pipeline pipe;
    private Element dec;

    private long duration;
    private final Container container;


    private XOverlay xoverlay;

    private final File videoFile;
    private final VideoEffect videoEffect;

    private EventListener listener;

    GstExternalMedia(File videoFile, Container container) throws Exception {
        this(videoFile, container, VideoEffect.NONE);
    }

    GstExternalMedia(File videoFile, Container container, VideoEffect videoEffect) throws Exception {

        this.videoFile = videoFile;
        this.container = container;        
        this.videoEffect = videoEffect;
    }


    private void setupPipeline(final File video) throws Exception {

        String pipedesc = String.format("filesrc name=oggsrc location=%s ! decodebin2 name=dec  " +
                "dec. ! ffmpegcolorspace ! videoflip method=%s ! %s name=videoSink force-aspect-ratio=true " +
                "dec. ! audioconvert ! autoaudiosink ", video.getAbsolutePath().replace('\\', '/'), videoEffect.method, overlayFactory);

        pipe = Pipeline.launch(pipedesc);

        videoSink = pipe.getElementByName("videoSink");

        canvas.setBackground(Color.BLACK);

        container.add(canvas, BorderLayout.CENTER);

        logger.info("gst-launch {}", pipedesc);

        pipe.getBus().connect((Bus.INFO) this);
        pipe.getBus().connect((Bus.WARNING) this);
        pipe.getBus().connect((Bus.ERROR) this);
        pipe.getBus().connect((Bus.STATE_CHANGED) this);

        if (!Platform.isWindows()) {
            pipe.getBus().setSyncHandler(new BusSyncHandler() {

                @Override
                public BusSyncReply syncMessage(Message msg) {
                    Structure s = msg.getStructure();
                    if (s == null || !s.hasName("prepare-xwindow-id")) {
                        return BusSyncReply.PASS;
                    }
                    xoverlay = XOverlay.wrap(videoSink);
                    xoverlay.setWindowHandle(Native.getComponentID(canvas));
                    return BusSyncReply.DROP;
                }
            });
        } else {

            xoverlay = XOverlay.wrap(videoSink);

            xoverlay.setWindowHandle(Native.getComponentID(canvas));
        }

        dec = pipe.getElementByName("dec");

        dec.connect((Element.PAD_ADDED) this);
        dec.connect((Element.NO_MORE_PADS) this);
    }


    @Override
    public void noMorePads(Element element) {
        if (duration == 0) {
            duration = pipe.queryDuration(TimeUnit.MILLISECONDS);
            logger.info("duration: {}", duration);
        }

        setDuration(duration);
    }

    private void setDuration(long duration) {

        if (this.duration == 0 && duration != 0) {
            this.duration = duration;
            logger.info("duration: {}", duration);
            listener.onEvent(EventType.DURATION);
        }
    }

    @Override
    public void padAdded(Element element, Pad pad) {

        Caps caps = pad.getCaps();

        logger.info("new pad with caps {}", caps);


    }


    @Override
    public void stop() {
        logger.info("stopping pipeline..");
        pipe.stop();        
        container.remove(canvas);
    }


    @Override
    public void start() {

        try {
            setupPipeline(videoFile);
        } catch (Exception e) {
            logger.error("failed to setup pipeline", e);
            return;
        }

        pipe.play();
    }

    @Override
    public void pause() {
        pipe.pause();
    }

    @Override
    public void play() {
        pipe.play();
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
                case PLAYING:
                    listener.onEvent(EventType.PLAY);
                    break;
                case PAUSED:
                    listener.onEvent(EventType.PAUSE);

                    if (duration == 0) {
                        setDuration(pipe.queryDuration(TimeUnit.MILLISECONDS));
                    }
                    break;
                case NULL:
                    listener.onEvent(EventType.STOP);
                    break;
                default:
                    break;
            }
        }
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
        return pipe == null ? 0 : pipe.queryPosition(TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean setTime(long time) {

        if (pipe != null) {
            return pipe.seek(time, TimeUnit.MILLISECONDS);
        }

        return false;
    }

    @Override
    public boolean isPlaying() {
        return pipe != null && pipe.isPlaying();
    }

    @Override
    public boolean setRate(double rate) {
        if (pipe != null) {
            return pipe.seek(rate, Format.TIME, SeekFlags.FLUSH | SeekFlags.KEY_UNIT, SeekType.CUR, 0, SeekType.NONE, -1);
        }

        return false;
    }

    @Override
    public boolean step() {
        return false;
    }
}
