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

package org.nargila.robostroke;

import org.nargila.robostroke.param.ParameterBusEventData;


public class BusEvent {

	public enum Type {
		UUID,
		STROKE_DROP_BELOW_ZERO,
		STROKE_RISE_ABOVE_ZERO,
		STROKE_POWER_START,
		ROWING_STOP(false, new DataSerializer() {

			@Override
			protected String doSerialize(Object data) {
				Object[] vals = (Object[]) data;
				/* stopTimestamp, distance, splitTime, travelTime, strokeCount */
				return String.format("%d %f %d %d %d", vals);
			}
			
			@Override
			public Object doParse(String s) {
				String[] tokens = s.split(" ");
				/* stopTimestamp, distance, splitTime, travelTime, strokeCount */
				return new Object[] {new Long(tokens[0]), new Float(tokens[2]), new Long(tokens[0]), new Long(tokens[0]), new Integer(tokens[0])};
			}			
		}),
		ROWING_START_TRIGGERED,
		ROWING_START(false, new LONG()),
		ROWING_COUNT(false, new INT()),
		PARAMETER_CHANGE(false, new PARAMETER()),
		STROKE_POWER_END(false, new FLOAT()),
		STROKE_RATE(false, new INT()), 
		STROKE_DECELERATION_TRESHOLD, 
		STROKE_ACCELERATION_TRESHOLD, 
		STROKE_ROLL(false, new FLOAT_ARR()),
		RECOVERY_ROLL(false, new FLOAT_ARR()), 
		FREEZE_TILT(true, new BOOLEAN()), 
		HEART_BPM(true, new INT()), 
		IMMEDIATE_DISTANCE_REQUESTED,
		BOOKMARKED_DISTANCE(false, new DistanceEventSerializer()), 				
		ROWING_START_DISTANCE(false, new DistanceEventSerializer()), 
		CRASH_STACK, 
		REPLAY_PROGRESS,
		REPLAY_SKIPPED,
		LOGFILE_VERSION(false, new INT());

		private static final class DistanceEventSerializer extends DataSerializer {
			@Override
			protected String doSerialize(Object data) {
				Object[] vals = (Object[]) data;
				/* travelTime, travelDistance */
				return String.format("%d %f", vals);
			}

			@Override
			public Object doParse(String s) {
				String[] tokens = s.split(" ");
				return new Object[] {new Long(tokens[0]), new Float(tokens[1])};
			}
		}
		
		public static abstract class DataSerializer {
			Object parse(String s) {
				if (s.equals("null")) {
					return null;
				} else {
					return doParse(s);
				}
			}

			String serialize(Object data) {
				if (data == null) {
					return "null";
				} else {
					return doSerialize(data);
				}
			}

			protected String doSerialize(Object data) {
				return data.toString();
			}

			protected abstract Object doParse(String s);
		}

		private static class BOOLEAN extends DataSerializer {

			@Override
			public Object doParse(String s) {
				return new Boolean(s);
			}
		};

		private static final class LONG extends DataSerializer {

			@Override
			public Object doParse(String s) {
				return new Long(s);
			}
		};

		private static final class INT extends DataSerializer {

			@Override
			public Object doParse(String s) {
				return new Integer(s);
			}
		};

		private static final class FLOAT extends DataSerializer {

			@Override
			public Object doParse(String s) {
				return new Float(s);
			}
		};

		private static final class FLOAT_ARR extends DataSerializer {

			@Override
			public Object doParse(String s) {
				String[] sarr = s.split(",");
				float[] res = new float[sarr.length];

				for (int i = 0; i < sarr.length; ++i) {
					res[i] = new Float(sarr[i]);
				}
				return res;
			}

			@Override
			protected String doSerialize(Object data) {

				String sdata = "";

				int i = 0;
				for (float f : ((float[]) data)) {
					if (i++ != 0) {
						sdata += ",";
					}
					sdata += f;
				}

				return sdata;
			}
		}

		private static final class PARAMETER extends DataSerializer {
			@Override
			public Object doParse(String s) {
				return new ParameterBusEventData(s);
			}
		}

		public final boolean isReplayableEvent;
		private final DataSerializer dataParser;
		
		private Type(boolean isReplayableEvent, DataSerializer dataParser) {
			this.isReplayableEvent = isReplayableEvent;
			this.dataParser = dataParser;
		}
		
		private Type() {
			this(false, null);
		}
		
	}
	
	public final Type type;
	public final long timestamp;
	public final Object data;
		
	public BusEvent(Type type, long timestamp, Object data) {
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

	public static BusEvent create(Type type, long timestamp, Object data) {
		return new BusEvent(type, timestamp, data);
	}

	public static BusEvent create(Type type, long timestamp, String str) {
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