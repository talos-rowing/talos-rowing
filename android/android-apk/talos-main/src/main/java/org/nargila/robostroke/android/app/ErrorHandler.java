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
package org.nargila.robostroke.android.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import org.acra.ACRA;
import org.nargila.robostroke.data.DataRecord.Type;

public class ErrorHandler implements UncaughtExceptionHandler {

  private final RoboStrokeActivity owner;


  public ErrorHandler(RoboStrokeActivity owner) {
    this.owner = owner;
  }


  @Override
  public void uncaughtException(Thread t, Throwable e) {

    reportOnBus(e);

    ACRA.getErrorReporter().uncaughtException(t, e);

  }


  private void reportOnBus(Throwable e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    e.printStackTrace(pw);

    owner.getRoboStroke().getBus().fireEvent(Type.CRASH_STACK, sw.toString().replace("\n", "\\n"));

    try {
      Thread.sleep(500); // time enough for logger - if active - to flush to sdcard?
    } catch (InterruptedException e1) {
      // tough luck - have no idea what to do or why should get here
    }
  }
}
