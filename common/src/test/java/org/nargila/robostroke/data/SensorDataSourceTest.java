package org.nargila.robostroke.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SensorDataSourceTest {

  private SensorDataSource dataSrc;

  @Before
  public void setUp() {
    dataSrc = new SensorDataSource();
  }

  @Test
  public void testSinkWeight() {
    SensorDataSink ds0 = new SensorDataSink() {

      @Override
      public void onSensorData(long timestamp, Object value) {

      }
    };

    SensorDataSink ds1 = new SensorDataSink() {

      @Override
      public void onSensorData(long timestamp, Object value) {

      }
    };

    SensorDataSink ds2 = new SensorDataSink() {

      @Override
      public void onSensorData(long timestamp, Object value) {

      }
    };

    SensorDataSink ds3 = new SensorDataSink() {

      @Override
      public void onSensorData(long timestamp, Object value) {

      }
    };

    SensorDataSink ds4 = new SensorDataSink() {

      @Override
      public void onSensorData(long timestamp, Object value) {

      }
    };

    dataSrc.addSensorDataSink(ds0, 0.0);
    dataSrc.addSensorDataSink(ds3);
    dataSrc.addSensorDataSink(ds4);
    dataSrc.addSensorDataSink(ds1, 0.0);
    dataSrc.addSensorDataSink(ds2, 0.1);

    assertEquals(ds0, dataSrc.sinkList.get(0));
    assertEquals(ds1, dataSrc.sinkList.get(1));
    assertEquals(ds2, dataSrc.sinkList.get(2));
    assertEquals(ds3, dataSrc.sinkList.get(3));
    assertEquals(ds4, dataSrc.sinkList.get(4));
  }

}
