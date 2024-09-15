/*
 * Copyright (c) 2012 Tal Shalif
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

package org.nargila.robostroke.common;

import java.io.Closeable;
import java.io.IOException;

public abstract class DataConverter<IN extends Closeable, OUT extends Closeable> implements Runnable {

  protected final IN in;
  protected final OUT out;

  private final long inputLength;

  private boolean cancelled;

  private boolean good;
  private Exception error;

  public DataConverter(IN in, OUT out, long inputLength) {
    this.in = in;
    this.out = out;
    this.inputLength = inputLength;
  }

  public void cancel() {
    cancelled = true;
  }

  protected abstract int processNext() throws IOException;

  public void run() {

    onStart();

    try {

      long accum = 0;

      for (int i =  processNext(); !cancelled && i != -1; i =  processNext()) {

        accum += i;

        cancelled = !onProgress(accum / (double) inputLength);
      }

      if (!cancelled) {
        good = true;
      }
    } catch (Exception e) {
      error = e;
      onError(e);
    } finally {

      if (in != null) try {in.close();} catch (Exception e) {}
      if (out != null) try {out.close();} catch (Exception e) {}

      onFinish();
    }
  }

  public boolean isGood() {
    return good;
  }

  public Exception getError() {
    return error;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  protected void onStart() {}
  protected abstract boolean onProgress(double d);
  protected void onFinish() {}
  protected void onError(Exception e) {}
}
