package org.nargila.robostroke.media.vlc;

import com.sun.jna.Memory;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BufferedImageMediaPlayer extends DirectMediaPlayerComponent {

    private final static class RV32BufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }
    }


    private BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(1, 1);
    private int[] rgbBuffer = new int[image.getWidth() * image.getHeight()];
    private final ReentrantReadWriteLock imageLock = new ReentrantReadWriteLock();

    public BufferedImageMediaPlayer() {
        super(new RV32BufferFormatCallback());
    }

    protected abstract void onImageChanged(BufferedImage image, long timestamp);

    public BufferedImage lockImage() {
        imageLock.readLock().lock();
        return image;
    }

    public void releaseImage(BufferedImage image) {
        imageLock.readLock().unlock();
    }

    @Override
    public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {

        imageLock.writeLock().lock();

        try {

            int width = bufferFormat.getWidth();
            int height = bufferFormat.getHeight();

            if (width != image.getWidth() || height != image.getHeight()) {
                image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
                rgbBuffer = new int[image.getWidth() * image.getHeight()];
            }

            nativeBuffers[0].getByteBuffer(0L, nativeBuffers[0].size()).asIntBuffer().get(rgbBuffer, 0, bufferFormat.getHeight() * bufferFormat.getWidth());
            image.setRGB(0, 0, image.getWidth(), image.getHeight(), rgbBuffer, 0, image.getWidth());

            onImageChanged(image, mediaPlayer.getTime());
        } finally {
            imageLock.writeLock().unlock();
        }
    }
}
