package org.nargila.robostroke.data;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.RoboStrokeEventBus;

public abstract class RecordDataInput extends SensorDataInputBase {
	
	private long currenSeekId;
	private boolean seakable;
	
	protected final RoboStroke roboStroke;
	protected final RoboStrokeEventBus bus;

	public RecordDataInput(RoboStroke roboStroke) {
		this.roboStroke = roboStroke;
		this.bus = roboStroke.getBus();
	}

	public void setSeakable(boolean seakable) {
		this.seakable = seakable;
	}
	
	public boolean isSeakable() {
		return seakable;
	}
	
	/**
	 * set play pos
	 * @param pos
	 */	
	public final void setPos(final double pos) {

		if (pos < 0 || pos > 1.0) {
			throw new IllegalArgumentException("pos must be a float between 0 and 1.0");
		}
		
		if (seakable) {
			onSetPosPending(pos);

			new Thread("deffered seek job") {

				long seekId = ++currenSeekId;				

				@Override
				public void run() {

					try {
						sleep(200);

						if (seekId == currenSeekId) {
							bus.fireEvent(DataRecord.Type.REPLAY_SKIPPED, null);
							onSetPosFinish(pos);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	protected void onSetPosPending(double pos) {}	
	protected abstract void onSetPosFinish(double pos);	
	
	public void playRecord(DataRecord record) {
			switch (record.type) {
			case GPS:
				gpsDataSource.pushData(record.timestamp, record.data);		
				break;
			case ACCEL:
				accelerometerDataSource.pushData(record.timestamp, record.data);
				break;
			case ORIENT:
				orientationDataSource.pushData(record.timestamp, record.data);
				break;
			default:				
				if (record.type.isReplayableEvent) {
					if (record.type.isBusEvent && bus != null) {
						bus.fireEvent(record);
					}					
				}
				
				break;
			}
	}

	public void playRecord(String line) {
		playRecord(line, null);
	}
	
	public void playRecord(String line, String endOfRecMark) {
		
		if (endOfRecMark != null) {
			line = line.substring(0, line.length() - endOfRecMark.length());
		}
		
		String[] vals = line.split(" +");

		DataRecord.Type type = DataRecord.Type.valueOf(vals[0]);

		if (type.isParsableEvent) {		  

			DataRecord record = DataRecord.create(type, Long.parseLong(vals[1]), vals[2]);
			
			playRecord(record);
		}
	}

}