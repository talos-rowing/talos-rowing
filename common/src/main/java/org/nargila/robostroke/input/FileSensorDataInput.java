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

import org.nargila.robostroke.BusEvent;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.SessionRecorderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SensorDataInput implementation for replaying data from a file 
 * @author tshalif
 *
 */
public class FileSensorDataInput extends SensorDataInputBase implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(FileSensorDataInput.class);
	
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
	
	private final RoboStrokeEventBus bus;
	
	private final long fileLength;

	private long lastProgressNotifyTime;
	
	public FileSensorDataInput(RoboStrokeEventBus bus, File dataFile) throws IOException {
		this.bus = bus;
		this.reader = new RandomAccessFile(dataFile, "r");
		fileLength = dataFile.length();
		
		checkVersion();

	}

	private void checkVersion() throws IOException, SessionFileVersionError {
		String line = reader.readLine();
		
		int version = -1;
		boolean validVersion = false;
		
		if (line != null) {
			String[] vals = readRecordLine(line);
			
			if (vals != null) {
				final InputType type = InputType.valueOf(vals[1]);

				switch (type) {
				case EVENT:
					BusEvent.Type eventType;
					
					try {
						eventType = BusEvent.Type.valueOf(vals[2]);
					} catch (IllegalArgumentException e) {
						break; // SessionFileVersionError() is thrown later
					}
					
					switch (eventType) {
					case LOGFILE_VERSION:
						version = new Integer(vals[4]);
						
						if (version == SessionRecorderConstants.LOGFILE_VERSION) {
							validVersion = true;
						}
						
						break;
					}
					break;
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
					
					if (bus != null) bus.fireEvent(BusEvent.Type.REPLAY_SKIPPED, null);

					continue;
				}

				long currentTimeMillis = System.currentTimeMillis();
				
				if (currentTimeMillis - lastProgressNotifyTime > 500) {
					
					lastProgressNotifyTime = currentTimeMillis;
					
					double progress = pos / (double)fileLength;
					
					if (bus != null) bus.fireEvent(BusEvent.Type.REPLAY_PROGRESS, progress);

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

	private void handleRecord(String line) throws InterruptedException {
		String[] vals = readRecordLine(line);

		if (vals != null) {
			logTimestamp = new Long(vals[0]);

			final InputType type = InputType.valueOf(vals[1]);
			final Object values;

			final long currentTime = System.currentTimeMillis();

			if (startTimeDiff == 0) {
				startTimeDiff = logTimestamp - currentTime;
			}

			long normalizedTime = logTimestamp - startTimeDiff;

			switch (type) {
			case GPS:
				values = parseGPS(vals);
				break;
			case ACCEL:
			case ORIENT:
				values = parseSensors(type, vals);
				break;
			case EVENT:
				if (bus != null) {
					handleBusEvent(vals);
				}
				return;
			default:
				throw new RuntimeException("HDIGH!");
			}


			if (normalizedTime > currentTime + 20) {					
				Thread.sleep(normalizedTime - currentTime);
			} else {
				Thread.yield();
			}


			switch (type) {
			case GPS:
				dispatchGpsEvent((double[])values);					
				break;
			case ACCEL:
			case ORIENT:
				dispatchSensorEvent(type, (float[])values);
				break;
			default:
				throw new RuntimeException("HDIGH!");
			}
		}
	}

	private String[] readRecordLine(String line) {
		int eorIdx;
		if ((eorIdx = line.lastIndexOf(SessionRecorderConstants.END_OF_RECORD)) == -1) {
			return null;
		}

		String s = line.substring(0, eorIdx);

		String[] vals = s.split(" +");
		
		return vals;
	}

	private void handleBusEvent(String[] vals) {
		/* timestamp, EVENT <StrokeEvent.Type> timestamp data1,data2,data3.. */
		
		BusEvent.Type type = BusEvent.Type.valueOf(vals[2]);
		long timestamp = new Long(vals[3]);
		
		if (bus != null && type.isReplayableEvent) {
			BusEvent event = BusEvent.create(type, timestamp, vals[4]);

			bus.fireEvent(event);
		}
	}

	private void dispatchSensorEvent(InputType type, float[] values) {
		final long time = (long)values[0];
		final float[] vals = {
				values[1],
				values[2],
				values[3]								
		};
		
		switch (type) {
		case ACCEL:
			accelerometerDataSource.pushData(time, vals);
			break;
		case ORIENT:
			orientationDataSource.pushData(time, vals);
			break;
		}		
	}
	
	private void dispatchGpsEvent(double[] values) {

		long timestamp = (long) values[0]; 
		double[] vals = new double[DataIdx.GPS_ITEM_COUNT_];
		
		System.arraycopy(values, 1, vals, 0, vals.length);		
		gpsDataSource.pushData(timestamp, vals);		
	}

	private float[] parseSensors(InputType type, String[] vals) {
		float[] res = {
				Long.parseLong(vals[2]),
				Float.parseFloat(vals[3]),
				Float.parseFloat(vals[4]),
				Float.parseFloat(vals[5])				
		};
		
		return res;
	}

	private double[] parseGPS(String[] vals) {
		double[] res = new double[DataIdx.GPS_ITEM_COUNT_ + 1];
		final int startIdx = 3;
		res[0] = Long.parseLong(vals[2]); // time
		res[DataIdx.GPS_LAT + 1] = 		Float.parseFloat(vals[DataIdx.GPS_LAT + startIdx]); // alt
		res[DataIdx.GPS_LONG + 1] = 		Float.parseFloat(vals[DataIdx.GPS_LONG + startIdx]); // long
		res[DataIdx.GPS_ALT + 1] = 		Float.parseFloat(vals[DataIdx.GPS_ALT + startIdx]); // alt	
		res[DataIdx.GPS_SPEED + 1] = 		Float.parseFloat(vals[DataIdx.GPS_SPEED + startIdx]); // speed	
		res[DataIdx.GPS_BEARING + 1] = 		Float.parseFloat(vals[DataIdx.GPS_BEARING + startIdx]); // bearing	
		res[DataIdx.GPS_ACCURACY + 1] = Float.parseFloat(vals[DataIdx.GPS_ACCURACY + startIdx]); // accuracy		
		
		return res;
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
