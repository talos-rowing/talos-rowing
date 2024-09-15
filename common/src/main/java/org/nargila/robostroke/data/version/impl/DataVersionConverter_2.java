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

package org.nargila.robostroke.data.version.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.nargila.robostroke.common.DataConverter;
import org.nargila.robostroke.data.version.DataVersionConverter;
import org.nargila.robostroke.way.DistanceResolver;
import org.nargila.robostroke.way.DistanceResolverDefault;

public class DataVersionConverter_2 extends DataVersionConverter {

  protected static final float MIN_DISTANCE = 10;

  private boolean firstLine = true;

  private double accumulatedDistance = 0;

  @Override
  public File convert(File input) throws Exception {

    File res = File.createTempFile("talos-rowing-converter-2-", ".txt");

    final DistanceResolver distanceResolver = new DistanceResolverDefault();

    DataConverter<BufferedReader, Writer> converter = new DataConverter<BufferedReader, Writer>(new BufferedReader(new FileReader(input)), new FileWriter(res), input.length()) {

      private double[] lastValues;

      @Override
      protected boolean onProgress(double d) {

        if (progressListener != null) {
          return progressListener.onProgress(d);
        }

        return !cancelled;
      }

      @Override
      protected int processNext() throws IOException {

        String line = in.readLine();

        if (line != null) {

          if (firstLine) {
            line = line.replace("LOGFILE_VERSION -1 2", "LOGFILE_VERSION -1 3");
            firstLine = false;
          }


          String[] ss = line.split("\\s+");

          try {

            long timestamp = new Long(ss[0]);

            String type = ss[1];

            if (type.equals("GPS")) { // need to emmit ACCUMULATED_DISTANCE
              String[] fields = ss[ss.length - 1].replace("@@", "").split(",");

              double[] values = new double[fields.length];

              int i = 0;
              for (String s: fields) {
                values[i++] = new Double(s);
              }
              if (lastValues == null) {
                lastValues = values;
              } else {
                float distance = distanceResolver.calcDistance(lastValues, values);
                if (distance > MIN_DISTANCE) {
                  accumulatedDistance += distance;
                  lastValues = values;
                }
              }

              out.write(timestamp + " ACCUM_DISTANCE " + ss[2] + " " + accumulatedDistance + "@@\n");
            }

            out.write(line + "\n");

          } catch (Exception e) {
            throw new IOException("corrupt input file", e);
          }

          return line.length() + 1;
        }

        return -1;
      }
    };

    converter.run();

    if (converter.isGood()) {
      return res;
    } else if (converter.getError() != null) {
      throw converter.getError();
    }

    return null;
  }

}
