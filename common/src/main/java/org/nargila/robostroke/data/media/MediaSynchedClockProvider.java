package org.nargila.robostroke.data.media;

import org.nargila.robostroke.data.ClockProvider;

public class MediaSynchedClockProvider implements ClockProvider {

    private final ExternalMedia media;


    public MediaSynchedClockProvider(ExternalMedia media) {
        this.media = media;
    }

    @Override
    public long getTime() {
        return media.getTime();
    }

    @Override
    public void run() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset(long initialTime) {
    }
}
