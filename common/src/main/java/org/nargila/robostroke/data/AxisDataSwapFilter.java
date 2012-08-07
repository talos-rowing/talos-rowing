package org.nargila.robostroke.data;

public class AxisDataSwapFilter extends AxisDataFilter {

	
	public AxisDataSwapFilter(int dataIndex1, int dataIndex2) {
		super(dataIndex1, dataIndex2);
	}

	public AxisDataSwapFilter(SensorDataSink sink, int dataIndex1,
			int dataIndex2) {
		super(sink, dataIndex1, dataIndex2);
	}

	@Override
	protected void applyFilter(float[] vals) {
		float tmp = vals[dataIndex1];
		vals[dataIndex1] = vals[dataIndex2];
		vals[dataIndex2] = tmp;
	}
}
