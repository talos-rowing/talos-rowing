package org.nargila.robostroke.data;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchedFileDataInput extends FileDataInput {

    private static final Logger logger = LoggerFactory.getLogger(SynchedFileDataInput.class);

	private final TreeMap<Long /* timestamp */, Long /* file byte offset */> time2offsets = new TreeMap<Long, Long>();
	private final TreeMap<Long /* timestamp */, Long /* file byte offset */> offsets2time = new TreeMap<Long, Long>();
	private long lastSetTime;
	
	public SynchedFileDataInput(RoboStroke roboStroke, File dataFile, long startTime, int coundDownId) throws IOException {
		
		super(roboStroke, dataFile);
				
		scanOffsets(startTime, coundDownId);
		
		reader.seek(0);
	}

	private void scanOffsets(long startTime, int countDownId) throws IOException {
		
		long lastTimestamp = 0;
		long firstTimestamp = -1;
		long countDownTimestamp = 0;		
		long absoluteStartTime = startTime;
		
		do {
			
			long offset = reader.getFilePointer();
			
			String l = this.reader.readLine();
			
			if (l == null) {
				break;
			}
			
			Pair<Long /* record timestamp */, DataRecord> p = parseRecord(l, true);
			
			if (p == null) {
				continue;
			}
			
			if (firstTimestamp == -1) {
				
				firstTimestamp = p.first;
				
				if (countDownId == -1) {
					countDownTimestamp = firstTimestamp; 
				}
			}
			
			switch (p.second.type) {
			
			case RECORDING_COUNTDOWN:				
				if (countDownTimestamp == 0) {
					
					Object[] data = (Object[])p.second.data;
					
					if (countDownId == (Integer)data[1]) {
						countDownTimestamp = p.first;
						long countDownTimestampDiff = countDownTimestamp - firstTimestamp;
						
				        absoluteStartTime = startTime - countDownTimestampDiff;

						logger.info("found count down {} at offset {}", countDownId, countDownTimestamp);
					}					
				}
				break;
				
			case ACCEL:

				if (countDownTimestamp != 0) {

					long ts = p.first;

					if (ts - lastTimestamp > 30) {					

						long key = ts - firstTimestamp + absoluteStartTime;

						time2offsets.put(key, offset);
						offsets2time.put(offset, key);

						lastTimestamp = ts;
					}
				}
				
				break;
				default:
					break;
			}
		} while (true);
		        
		
		if (countDownTimestamp == 0) {
			throw new IllegalStateException("could not find count down marker event " + countDownId);			
		}
		
		logger.info("scanned {} offsets", time2offsets.size());
		
        
		long startTimeDiff = firstTimestamp - absoluteStartTime;
        
        logger.info("setting start at {}ms from start", absoluteStartTime);
        
		setStartTimeDiff(startTimeDiff);
	}
	

	protected double time2pos(long timestamp) {
		
		Entry<Long, Long> e = time2offsets.lowerEntry(timestamp);
		
		double pos = 0.0;
		
		if (e != null) {			
			long offset = e.getValue();
			pos = (double)offset / fileLength;
		}
		
		logger.debug("translated timestamp {} to pos {}", timestamp, pos);

		return pos;
	}


	@Override
	protected long getCurrentTime() {
		return System.currentTimeMillis() - lastSetTime;
	}
}
