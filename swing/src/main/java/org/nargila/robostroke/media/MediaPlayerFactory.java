package org.nargila.robostroke.media;

import org.nargila.robostroke.app.Settings;
import org.nargila.robostroke.data.media.ExternalMedia;
import org.nargila.robostroke.data.media.ExternalMedia.EventListener;
import org.nargila.robostroke.data.media.ExternalMedia.MediaFramework;
import org.nargila.robostroke.data.media.ExternalMedia.VideoEffect;
import org.nargila.robostroke.media.vlc.VlcExternalMedia;
import org.nargila.robostroke.media.vlc.VlcFindQrMarkPipeline;

import java.awt.*;
import java.io.File;

public class MediaPlayerFactory {

    private MediaPlayerFactory() {
    }

    public static FindQrMarkPipeline createFindQrMarkPipeline(File video) throws Exception {

        switch (getFramework()) {
            case GST:
                throw new AssertionError("GST support removed");
            case VLC:
                return new VlcFindQrMarkPipeline(video);
            default:
                throw new AssertionError("HDIGH!");
        }
    }

    private static MediaFramework getFramework() {
        return Settings.getInstance().getMediaFramework();
    }

    public static ExternalMedia createMediaPlayer(File videoFile, Container container, VideoEffect videoEffect, EventListener eventListener) throws Exception {
        ExternalMedia res;

        switch (getFramework()) {
            case GST:
                throw new AssertionError("GST support removed");
            case VLC:
                res = new VlcExternalMedia(videoFile, container, videoEffect);
                break;
            default:
                throw new AssertionError("HDIGH!");
        }

        res.addEventListener(eventListener);

        return res;
    }
}
