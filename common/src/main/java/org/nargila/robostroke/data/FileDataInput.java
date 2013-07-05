/*
 * Copyright (c) 2011 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.nargila.robostroke.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.DataRecord.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SensorDataInput implementation for replaying data from a file 
 * @author tshalif
 *
 */
public class FileDataInput extends RecordDataInput implements Runnable {

    private final boolean batchMode = Boolean.getBoolean(FileDataInput.class.getName() + ".batchMode");

    private static final Logger logger = LoggerFactory.getLogger(FileDataInput.class);

    private static final int SKIP_BYTES = 300;

    protected final RandomAccessFile reader;

    private float skipRequested = 0;

    private double setPosRequested = -1;

    private boolean paused;

    private Thread runThread;

    private boolean requestStop;

    protected final long fileLength;

    private long lastProgressNotifyTime;

    private final File dataFile;

    private ClockProvider clockProvider = new SystemClockProvider();

    private boolean resetClockRequired;

    private long startTimeOffset;

    private final long firstTimestamp;
    
    public FileDataInput(RoboStroke roboStroke, File dataFile) throws IOException {
        super(roboStroke);
        
        this.dataFile = dataFile;
        this.reader = new RandomAccessFile(dataFile, "r");
        fileLength = dataFile.length();

        setSeakable(true);

        firstTimestamp = checkVersion();

    }

    public void setClockProvider(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }
    
    public File getDataFile() {
        return dataFile;
    }

    protected void setStartTimeOffset(long startTimeOffset) {
        this.startTimeOffset = startTimeOffset;
    }

    private long checkVersion() throws IOException, SessionFileVersionError {
        
        String line = reader.readLine();

        int version = -1;
        
        boolean validVersion = false;

        long firstTimestamp = 0;
        
        if (line != null) {
            String[] vals = readRecordLine(line);

            if (vals != null) {
                final DataRecord.Type type;

                try {
                    type = DataRecord.Type.valueOf(vals[1]);

                    switch (type) {
                        case LOGFILE_VERSION:
                            version = new Integer(vals[3]);
                            firstTimestamp = new Long(vals[0]);
                            if (version == SessionRecorderConstants.LOGFILE_VERSION) {
                                validVersion = true;
                            }

                            break;
                        default:
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    // SessionFileVersionError() is thrown later
                }
            }
        }

        if (!validVersion) {
            throw new SessionFileVersionError(version);
        }		
        
        return firstTimestamp;
    }

    @Override
    public void run() {
        String l = "";


        while (!requestStop) {
            try {
                Thread.yield();

                long pos = reader.getFilePointer();

                if (setPosRequested != -1 || skipRequested != 0) {

                    if (setPosRequested != -1) {
                        pos = (long)(fileLength * setPosRequested);
                    } else {
                        assert skipRequested != 0;

                        pos += -skipRequested * SKIP_BYTES;
                    }

                    pos = Math.max(Math.min(reader.length() - 1,pos), 0);
                    reader.seek(pos);
                    reader.readLine();
                    skipRequested = 0;
                    setPosRequested = -1;

                    resetClockRequired = true;

                    if (bus != null) bus.fireEvent(DataRecord.Type.REPLAY_SKIPPED, null);

                    continue;
                }

                long currentTimeMillis = System.currentTimeMillis();

                if (currentTimeMillis - lastProgressNotifyTime > 500) {

                    lastProgressNotifyTime = currentTimeMillis;

                    double progress = calcProgress();

                    if (bus != null) bus.fireEvent(DataRecord.Type.REPLAY_PROGRESS, progress);

                }

                long lastFilePos = reader.getFilePointer();

                if ((l = reader.readLine()) == null) { 
                    continue;
                }

                handleRecord(l, lastFilePos);

            } catch (IOException e) {
                errorListener.onError(new Exception("can not read data", e));
                break;
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) { // probably corrupt record, try to continue anyway
                long pos;
                try {
                    pos = reader.getFilePointer();
                } catch (IOException e1) {
                    pos = -1;
                }
                logger.error(String.format("error while reading record from %s near byte offset %d [%s]", getDataFile(), pos, l), e);
                continue;
            }
        }
    }

    protected double calcProgress() throws IOException {
        return reader.getFilePointer() / (double)fileLength;
    }

    public static Pair<Long /* record timestamp */, DataRecord> parseRecord(String line) {
        return parseRecord(line, false);
    }

    public static Pair<Long /* record timestamp */, DataRecord> parseRecord(String line, boolean force) {

        String[] vals = readRecordLine(line);

        if (vals != null) {

            Long logTimestamp = new Long(vals[0]);

            DataRecord.Type type = DataRecord.Type.valueOf(vals[1]);

            if ((type.isReplayableEvent || force) && type.isParsableEvent) {
                return Pair.create(logTimestamp, DataRecord.create(type, Long.parseLong(vals[2]), vals[3]));
            }			
        }

        return null;
    }

    private void handleRecord(String line, long lastReaderPos) throws Exception {

        Pair<Long /* record timestamp */, DataRecord> p = parseRecord(line);

        if (p != null) {

            handleRecord(p.first, p.second, lastReaderPos);
        }
    }

    private void handleRecord(long logTimestamp, DataRecord record, long lastReaderPos) throws Exception {

        long normalizedLogfileTime = logTimestamp - firstTimestamp + startTimeOffset;

        if (resetClockRequired) {
            clockProvider.reset(normalizedLogfileTime);
            resetClockRequired = false;
        }
        
        final long currentTime = getCurrentTime();

        if (!batchMode && normalizedLogfileTime > currentTime + 20) {					
            logger.debug("data time {} later than current time {} - too soon to play, putting data back in reader", normalizedLogfileTime, currentTime);				
            reader.seek(lastReaderPos);
            Thread.sleep(50);
            return;
        } else {
            Thread.yield();
        }

        playRecord(record);
    }

    private long getCurrentTime() {
        return clockProvider.getTime();
    }

    private static String[] readRecordLine(String line) {
        int eorIdx;
        if ((eorIdx = line.lastIndexOf(SessionRecorderConstants.END_OF_RECORD)) == -1) {
            return null;
        }

        String s = line.substring(0, eorIdx);

        String[] vals = s.split(" +");

        return vals;
    }


    @Override
    public void skipReplayTime(float velocityX) {
        if (!paused) {
            skipRequested = velocityX;
        }

    }

    @Override
    protected void onSetPosFinish(double pos) {

        if (pos < 0 || pos > 1.0) {
            throw new IllegalArgumentException("pos must be a float between 0 and 1.0");
        }

        setPosRequested = pos;		
    }

    @Override
    public synchronized void setPaused(boolean paused) {

        if (this.paused != paused) {
            logger.info("setting paused = {}", paused);
            
            
            this.paused = paused;

            Type event;
            
            if (paused) {
                event = DataRecord.Type.REPLAY_PAUSED;
                clockProvider.stop();
            } else {
                event = DataRecord.Type.REPLAY_PLAYING;
                clockProvider.run();
            }

            if (bus != null) bus.fireEvent(event, null);
        }
    }

    @Override
    public synchronized void start() {

        super.start();

        runThread = new Thread(this, "MocDataFeeder") {
            {
                setDaemon(true);
            }
        };

        runThread.start();
        
        clockProvider.reset(startTimeOffset);
        clockProvider.run();
    }

    @Override
    public synchronized void stop() {
        if (runThread != null) {
            requestStop = true;
            runThread.interrupt();
            try {
                runThread.join();
            } catch (InterruptedException e) {
            }

            runThread = null;
        }

        super.stop();
    }
}
