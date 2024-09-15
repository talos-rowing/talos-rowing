/*
 * Copyright (c) 2024 Tal Shalif
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.SensorBinder;
import org.nargila.robostroke.data.DataRecord.Type;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterBusEventData;

public class SessionRecorder extends SensorBinder implements SessionRecorderConstants {

  private ErrorListener errorListener;
  private BufferedWriter logger;
  private boolean initialized;

  public SessionRecorder(RoboStroke roboStroke) {
    super(roboStroke);
  }

  public synchronized void setDataLogger(File file) throws IOException {
      if (logger != null) {
        disconnect();
        logger.close();
        logger = null;
      }

      initialized = false;

      if (file != null) {
        logger = new BufferedWriter(new FileWriter(file));
        connect();
      }
  }

  private void initDataLogger() {

    initialized = true;

    logEvent(new DataRecord(Type.LOGFILE_VERSION, -1, LOGFILE_VERSION));

    for (Parameter param: roboStroke.getParameters().getParamMap().values()) {

      logEvent(DataRecord.create(DataRecord.Type.SESSION_PARAMETER, -1,
          new ParameterBusEventData(param.getId() + "|" + param.convertToString())));
    }
  }


  @Override
  protected synchronized void onSensorData(DataRecord record) {
    logEvent(record);
  }

  public void setErrorListener(ErrorListener errorListener) {
    this.errorListener = errorListener;
  }

  @Override
  public synchronized void onBusEvent(DataRecord event) {

    if (event.type == DataRecord.Type.RECORDING_START) {
      initDataLogger();
    }

    logEvent(event);
  }

  private synchronized void logEvent(DataRecord event) {

    if (initialized) {
      StringBuffer sb = new StringBuffer();
      sb.append(System.currentTimeMillis()).append(" ")
      .append(event);

      try {
        logger.write(sb.toString());
        logger.write(END_OF_RECORD + "\n");

        if (event.type == Type.CRASH_STACK) {
          logger.flush();
        }
      } catch (IOException e) {
        if (errorListener != null) {
          errorListener.onError(e);
        }
      }
    }
  }
}
