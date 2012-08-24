package org.nargila.robostroke.data;

public class AxisDataReverseFilter extends AxisDataFilter {
	
	public AxisDataReverseFilter(int dataIndex1, int dataIndex2) {
		super(dataIndex1, dataIndex2);
	}

	public AxisDataReverseFilter(SensorDataSink sink, int dataIndex1, int dataIndex2) {
		super(sink, dataIndex1, dataIndex2);
	}

	
	@Override
	protected void applyFilter(float[] vals) {
		if (dataIndex1 != -1) {
			vals[dataIndex1] *= -1;
		}
		
		if (dataIndex2 != -1) {
			vals[dataIndex2] *= -1;
		}
	}
}
