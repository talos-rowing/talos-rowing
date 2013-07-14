package org.nargila.robostroke.media;

import org.nargila.robostroke.common.Pair;

public interface FindQrMarkPipeline {

    public void stop();

    public Pair<Integer, Long> findMark(int timeoutSeconds)
            throws Exception;

}