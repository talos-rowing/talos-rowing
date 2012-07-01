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


package org.nargila.robostroke.input;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.SessionRecorderConstants;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SensorDataInput implementation for replaying data from a file 
 * @author tshalif
 *
 */
public class FileDataInput extends RecordDataInput implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(FileDataInput.class);
	
	private static final int SKIP_BYTES = 300;
	
	private final RandomAccessFile reader;
	private long startTimeDiff;
	long logTimestamp;
	
	private float skipRequested = 0;
	
	private double setPosRequested = -1;

	private boolean paused;

	private long pauseStart;

	private Thread runThread;

	private boolean requestStop;
	
	private final long fileLength;

	private long lastProgressNotifyTime;

	private final File dataFile;
	
	public FileDataInput(RoboStrokeEventBus bus, File dataFile) throws IOException {
		super(bus);
		this.dataFile = dataFile;
		this.reader = new RandomAccessFile(dataFile, "r");
		fileLength = dataFile.length();
		
		checkVersion();

	}

	public File getDataFile() {
		return dataFile;
	}
	
	private void checkVersion() throws IOException, SessionFileVersionError {
		String line = reader.readLine();
		
		int version = -1;
		boolean validVersion = false;
		
		if (line != null) {
			String[] vals = readRecordLine(line);
			
			if (vals != null) {
				final DataRecord.Type type;
				
				try {
					type = DataRecord.Type.valueOf(vals[1]);

					switch (type) {
					case LOGFILE_VERSION:
						version = new Integer(vals[3]);

						if (version == SessionRecorderConstants.LOGFILE_VERSION) {
							validVersion = true;
						}

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
	}
	
	@Override
	public void run() {
		String l;


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
					startTimeDiff = 0; // force re-adjust below
					
					if (bus != null) bus.fireEvent(DataRecord.Type.REPLAY_SKIPPED, null);

					continue;
				}

				long currentTimeMillis = System.currentTimeMillis();
				
				if (currentTimeMillis - lastProgressNotifyTime > 500) {
					
					lastProgressNotifyTime = currentTimeMillis;
					
					double progress = pos / (double)fileLength;
					
					if (!paused && bus != null) bus.fireEvent(DataRecord.Type.REPLAY_PROGRESS, progress);

				}
				
				if (paused || 
						(l = reader.readLine()) == null) { 
					continue;
				}
				
				handleRecord(l);
				
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
				logger.error(String.format("error while reading record near byte offset %d", pos), e);
				continue;
			}
		}
	}

	public static Pair<Long /* record timestamp */, DataRecord> parseRecord(String line) {
		
		String[] vals = readRecordLine(line);
		
		if (vals != null) {
			
			Long logTimestamp = new Long(vals[0]);
			
			DataRecord.Type type = DataRecord.Type.valueOf(vals[1]);

			if (type.isParsableEvent) {
				return Pair.create(logTimestamp, DataRecord.create(type, Long.parseLong(vals[2]), vals[3]));
			}			
		}
		
		return null;
	}
	
	private void handleRecord(String line) throws InterruptedException {
		
		Pair<Long /* record timestamp */, DataRecord> p = parseRecord(line);
		
		if (p != null) {
			handleRecord(p.first, p.second);
		}
	}

	private void handleRecord(long timestamp, DataRecord record)
			throws InterruptedException {

		logTimestamp = timestamp;

		final long currentTime = System.currentTimeMillis();

		if (startTimeDiff == 0) {
			startTimeDiff = logTimestamp - currentTime;
		}

		long normalizedTime = logTimestamp - startTimeDiff;


		if (normalizedTime > currentTime + 20) {					
			Thread.sleep(normalizedTime - currentTime);
		} else {
			Thread.yield();
		}


		playRecord(record);
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

	public void setPos(double pos) {

		if (pos < 0 || pos > 1.0) {
			throw new IllegalArgumentException("pos must be a float between 0 and 1.0");
		}
		
		setPosRequested = pos;		
	}
	
	@Override
	public void setPaused(boolean paused) {
		if (paused) {
			pauseStart = System.currentTimeMillis();
		} else {
			startTimeDiff -= System.currentTimeMillis() - pauseStart;
		}
		
		this.paused = paused;		
	}

	@Override
	public synchronized void start() {
	    runThread = new Thread(this, "MocDataFeeder") {
		    {
			setDaemon(true);
		    }
		};

		runThread.start();
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
	}
}
