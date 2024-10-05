package org.nargila.robostroke.media.vlc;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.media.FindQrMarkPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class VlcFindQrMarkPipeline implements FindQrMarkPipeline {

    private static final Logger logger = LoggerFactory.getLogger(VlcFindQrMarkPipeline.class);

    private final AtomicReference<Exception> finishSync = new AtomicReference<Exception>();

    private String mark;

    private ClockTime timestamp;

    private final DirectMediaPlayerComponent vlc;

    private final File video;

    private final MediaPlayer mp;

    public VlcFindQrMarkPipeline(File video) {

        VlcSetup.setupCheckVlc(null);

        vlc = new BufferedImageMediaPlayer() {
            @Override
            protected void onImageChanged(BufferedImage image, long timestamp) {

                if (findQrCode(image, timestamp)) {

                    synchronized (finishSync) {
                        finishSync.notifyAll();
                    }
                }
            }
        };

        this.video = video;
        mp = vlc.getMediaPlayer();
    }

    void start() {
        mp.playMedia(video.getAbsolutePath());
    }


    @Override
    public void stop() {

        synchronized (finishSync) {
            finishSync.notifyAll();
        }
    }

    private void doStop() {
//    mp.stop();
        vlc.release();
    }


    private boolean findQrCode(BufferedImage image, long time) {

        if (mark == null) {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            ClockTime t = ClockTime.fromMillis(time);
            try {

                Result result = new MultiFormatReader().decode(bitmap);
                mark = result.getText();

                timestamp = t;

                logger.info("mark: {}, timestamp: {}, pipetime: {}",
                        new Object[]{mark, timestamp.toMillis(),
                                vlc.getMediaPlayer().getTime()});

            } catch (NotFoundException e) {

                logger.info("timestamp: {}", t);

                return false;
            }
            return true;
        }

        return false;
    }


    @Override
    public Pair<Integer, Long> findMark(int timeoutSeconds) throws Exception {

        synchronized (finishSync) {
            start();
            finishSync.wait(timeoutSeconds * 1000);
        }

        logger.info("################ calling doStop()...");

        doStop();

        logger.info("################ done.");

        if (mark == null) {

            if (finishSync.get() != null) {
                throw finishSync.get();
            }

            throw new IllegalStateException("could not find QR sync mark in video within " + timeoutSeconds + " seconds");
        }

        return Pair.create(Integer.valueOf(mark.split(":")[1]), timestamp.toMillis());
    }

    public static void main(String[] args) throws Exception {

        final VlcFindQrMarkPipeline qrFind = new VlcFindQrMarkPipeline(new File(args[0]));

        qrFind.findMark(160);
    }
}
