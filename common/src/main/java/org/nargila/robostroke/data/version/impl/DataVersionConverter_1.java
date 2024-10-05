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

import org.nargila.robostroke.common.DataConverter;
import org.nargila.robostroke.data.version.DataVersionConverter;

import java.io.*;

public class DataVersionConverter_1 extends DataVersionConverter {

    private boolean firstLine = true;

    @Override
    public File convert(File input) throws Exception {

        File res = File.createTempFile("talos-rowing-converter-1-", ".txt");


        DataConverter<BufferedReader, Writer> converter = new DataConverter<BufferedReader, Writer>(new BufferedReader(new FileReader(input)), new FileWriter(res), input.length()) {

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
                    line = line.replace(" EVENT ", " ");

                    if (firstLine) {
                        line = line.replace("LOGFILE_VERSION -1 1", "LOGFILE_VERSION -1 2");
                        firstLine = false;
                    }

                    String[] ss = line.split("\\s+");
                    StringBuffer sb = new StringBuffer();

                    for (int i = 0; i < ss.length; ++i) {

                        switch (i) {
                            case 0:
                                break;
                            case 1:
                            case 2:
                            case 3:
                                sb.append(" ");
                                break;
                            default:
                                sb.append(",");
                                break;
                        }

                        sb.append(ss[i]);
                    }

                    line = sb.toString();

                    out.write(line + "\n");

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
