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




public class DataRecord {

	public enum Type {
		UUID,
		RECORDING_COUNTDOWN,
		STROKE_DROP_BELOW_ZERO,
		STROKE_RISE_ABOVE_ZERO,
		STROKE_POWER_START,
		ROWING_STOP(false, new DataRecordSerializer() {

			@Override
			protected String doSerialize(Object data) {
				Object[] vals = (Object[]) data;
				/* stopTimestamp, distance, splitTime, travelTime, strokeCount */
				return String.format("%d,%f,%d,%d,%d", vals);
			}
			
			@Override
			public Object doParse(String s) {
				String[] tokens = s.split(",");
				/* stopTimestamp, distance, splitTime, travelTime, strokeCount */
				return new Object[] {new Long(tokens[0]), new Float(tokens[1]), new Long(tokens[2]), new Long(tokens[3]), new Integer(tokens[4])};
			}			
		}),
		ROWING_START_TRIGGERED,
		ROWING_START(false, new DataRecordSerializer.LONG()),
		ROWING_COUNT(false, new DataRecordSerializer.INT()),
		PARAMETER_CHANGE(false, new DataRecordSerializer.PARAMETER()),
		SESSION_PARAMETER(true, new DataRecordSerializer.PARAMETER()),
		STROKE_POWER_END(false, true, new DataExporter() {

			@Override
			public String[] getColumnNames() {
				return new String[] {"power"};
			}

			@Override
			public Object[] exportData(Object data) {
				return new Object[]{data};
				
			}
		}, new DataRecordSerializer.FLOAT()),
		STROKE_RATE(false, true, new DataExporter() {

			@Override
			public String[] getColumnNames() {
				return new String[] {"stroke_rate"};
			}

			@Override
			public Object[] exportData(Object data) {
				return new Object[]{data};
			}
		}, new DataRecordSerializer.INT()), 
		STROKE_DECELERATION_TRESHOLD, 
		STROKE_ACCELERATION_TRESHOLD, 
		STROKE_ROLL(false, new DataRecordSerializer.FLOAT_ARR()),
		RECOVERY_ROLL(false, new DataRecordSerializer.FLOAT_ARR()), 
		ACCEL(true, false, new DataExporter() {

			@Override
			public String[] getColumnNames() {
				return new String[] {"x", "y", "z"};
			}

			@Override
			public Object[] exportData(Object data) {

				float[] fdata = (float[]) data;

				return new Object[]{fdata[0], fdata[1], fdata[2]};

			}
		}, new DataRecordSerializer.FLOAT_ARR()),
		ORIENT(true, false, new DataExporter() {

			@Override
			public String[] getColumnNames() {
				return new String[] {"azimuth", "pitch", "roll"};
			}

			@Override
			public Object[] exportData(Object data) {

				float[] fdata = (float[]) data;

				return new Object[]{fdata[0], fdata[1], fdata[2]};

			}
		}, new DataRecordSerializer.FLOAT_ARR()), 
		GPS(true, false, new DataExporter() {

			@Override
			public String[] getColumnNames() {
				return new String[] {"lat", "long", "alt", "speed", "bearing", "accuracy"};
			}

			@Override
			public Object[] exportData(Object data) {

				double[] ddata = (double[]) data;

				return new Object[]{ddata[0], ddata[1], ddata[2], ddata[3], ddata[4], ddata[5]};

			}
		}, new DataRecordSerializer.DOUBLE_ARR()), 
		WAY(false, true, new DataExporter() {

			@Override
			public String[] getColumnNames() {
				return new String[] {"distance", "speed", "accuracy"};
			}

			@Override
			public Object[] exportData(Object data) {

				double[] ddata = (double[]) data;

				return new Object[]{ddata[0], ddata[1], ddata[2]};

			}
		}, new DataRecordSerializer.DOUBLE_ARR()), 
		FREEZE_TILT(true, new DataRecordSerializer.BOOLEAN()), 
		HEART_BPM(true, new DataRecordSerializer.INT()), 
		IMMEDIATE_DISTANCE_REQUESTED,
		BOOKMARKED_DISTANCE(false, new DistanceEventSerializer()), 				
		ROWING_START_DISTANCE(false, new DistanceEventSerializer()), 
		CRASH_STACK, 
		REPLAY_PROGRESS,
		REPLAY_SKIPPED,
		LOGFILE_VERSION(false, new DataRecordSerializer.INT());

		private static final class DistanceEventSerializer extends DataRecordSerializer {
			@Override
			protected String doSerialize(Object data) {
				Object[] vals = (Object[]) data;
				/* travelTime, travelDistance */
				return String.format("%d,%f", vals);
			}

			@Override
			public Object doParse(String s) {
				String[] tokens = s.split(",");
				return new Object[] {new Long(tokens[0]), new Float(tokens[1])};
			}
		}
		
		public interface DataExporter {
			String[] getColumnNames();
			Object[] exportData(Object data);
		}
		
		public final boolean isReplayableEvent;
		public final boolean isParsableEvent;
		public final boolean isBusEvent;
		public final boolean isExportableEvent;
		private final DataRecordSerializer dataParser;
		private final DataExporter dataExporter;
		
		private Type(boolean isReplayableEvent, DataRecordSerializer dataParser) {
			this(isReplayableEvent, true, null, dataParser);
		}
		
		private Type(boolean isReplayableEvent, boolean isBusEvent, DataExporter dataExporter, DataRecordSerializer dataParser) {
			this.isReplayableEvent = isReplayableEvent;
			this.isBusEvent = isBusEvent;
			this.isExportableEvent = dataExporter != null;
			this.dataExporter = dataExporter;
			this.dataParser = dataParser;
			this.isParsableEvent = dataParser != null;
		}
		
		private Type() {
			this(false, true, null, null);
		}
		
		public DataExporter getDataExporter() {
			return dataExporter;
		}		
	}
	
	public final Type type;
	public final long timestamp;
	public final Object data;
		
	public DataRecord(Type type, long timestamp, Object data) {
		this.type = type;
		this.timestamp = timestamp;
		this.data = data;
	}
	
	@Override
	public String toString() {
		
		String sdata = dataToString();
		
		return "" + type + " " + timestamp + " " + sdata;
	}

	public String dataToString() {
		String sdata;
		
		if (type.dataParser != null) {
			sdata = type.dataParser.serialize(data);
		} else {
			sdata = String.valueOf(data);
		}
		return sdata;
	}

	public Object[] exportData() {
		if (type.isExportableEvent) {
			return type.dataExporter.exportData(data);
		}
		
		return null;
	}
	
	public static DataRecord create(Type type, long timestamp, Object data) {
		return new DataRecord(type, timestamp, data);
	}

	public static DataRecord create(Type type, long timestamp, String str) {
		if (type.dataParser != null) {
			Object data = type.dataParser.parse(str);
			return create(type, timestamp, data);
		} else {
			throw new UnsupportedOperationException(
					String
							.format(
									"StrokeEvent type %s does not have a serializer configured",
									type));
		}
	}
}