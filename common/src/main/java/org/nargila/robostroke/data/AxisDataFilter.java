package org.nargila.robostroke.data;

public abstract class AxisDataFilter extends SensorDataFilter {

    protected final int dataIndex1;
    protected final int dataIndex2;

    private boolean enabled;

    public AxisDataFilter(int dataIndex1, int dataIndex2) {
        this.dataIndex1 = dataIndex1;
        this.dataIndex2 = dataIndex2;
    }

    public AxisDataFilter(SensorDataSink sink, int dataIndex1, int dataIndex2) {
        super(sink);

        this.dataIndex1 = dataIndex1;
        this.dataIndex2 = dataIndex2;

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected Object filterData(long timestamp, Object value) {
        if (enabled) {
            applyFilter((float[]) value);
        }

        return value;
    }

    protected abstract void applyFilter(float[] value);
}
