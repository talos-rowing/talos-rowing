package org.nargila.robostroke.input;

import org.nargila.robostroke.RoboStrokeEventBus;

public abstract class RecordDataInput extends SensorDataInputBase {
	
	private long currenSeekId;
	
	protected final RoboStrokeEventBus bus;

	public RecordDataInput(RoboStrokeEventBus bus) {
		this.bus = bus;
	}

	
	/**
	 * set play pos
	 * @param pos
	 */	
	public final void setPos(final double pos) {

		if (pos < 0 || pos > 1.0) {
			throw new IllegalArgumentException("pos must be a float between 0 and 1.0");
		}
		
		
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
	
	protected void onSetPosPending(double pos) {}	
	protected abstract void onSetPosFinish(double pos);	
	
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