package org.nargila.robostroke.input;

import org.nargila.robostroke.RoboStrokeEventBus;

public abstract class RecordDataInput extends SensorDataInputBase {

	protected final RoboStrokeEventBus bus;

	public RecordDataInput(RoboStrokeEventBus bus) {
		this.bus = bus;
	}

	
	/**
	 * set play pos
	 * @param pos
	 */	
	public abstract void setPos(double pos);	
	
	public void playRecord(DataRecord record) {
		if (record.type.isReplayableEvent) {
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
			}
	
			if (record.type.isBusEvent && bus != null) {
				bus.fireEvent(record);
			}
		}
	}

}