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

package org.nargila.robostroke.data;

import org.nargila.robostroke.param.ParameterBusEventData;

public abstract class DataRecordSerializer {
    public static class BOOLEAN extends DataRecordSerializer {

        @Override
        public Object doParse(String s) {
            return Boolean.valueOf(s);
        }
    }

    public static final class LONG extends DataRecordSerializer {

        @Override
        public Object doParse(String s) {
            return Long.valueOf(s);
        }
    }

    public static final class INT extends DataRecordSerializer {

        @Override
        public Object doParse(String s) {
            return Integer.valueOf(s);
        }
    }

    public static final class FLOAT extends DataRecordSerializer {

        @Override
        public Object doParse(String s) {
            return Float.valueOf(s);
        }
    }

    public static final class DOUBLE extends DataRecordSerializer {

        @Override
        public Object doParse(String s) {
            return Double.valueOf(s);
        }
    }

    public static final class FLOAT_ARR extends DataRecordSerializer {

        private final String sep;

        public FLOAT_ARR() {
            this(",");
        }

        public FLOAT_ARR(String sep) {
            this.sep = sep;
        }

        @Override
        public Object doParse(String s) {
            String[] sarr = s.split(sep);
            float[] res = new float[sarr.length];

            for (int i = 0; i < sarr.length; ++i) {
                res[i] = Float.parseFloat(sarr[i]);
            }
            return res;
        }

        @Override
        protected String doSerialize(Object data) {

            String sdata = "";

            int i = 0;
            for (float f : ((float[]) data)) {
                if (i++ != 0) {
                    sdata += sep;
                }
                sdata += f;
            }

            return sdata;
        }
    }

    public static final class DOUBLE_ARR extends DataRecordSerializer {

        private final String sep;

        public DOUBLE_ARR() {
            this(",");
        }

        public DOUBLE_ARR(String sep) {
            this.sep = sep;
        }

        @Override
        public Object doParse(String s) {
            String[] sarr = s.split(sep);
            double[] res = new double[sarr.length];

            for (int i = 0; i < sarr.length; ++i) {
                res[i] = Double.parseDouble(sarr[i]);
            }
            return res;
        }

        @Override
        protected String doSerialize(Object data) {

            String sdata = "";

            int i = 0;
            for (double f : ((double[]) data)) {
                if (i++ != 0) {
                    sdata += sep;
                }
                sdata += f;
            }

            return sdata;
        }
    }

    public static final class PARAMETER extends DataRecordSerializer {
        @Override
        public Object doParse(String s) {
            return new ParameterBusEventData(s);
        }
    }

    public Object parse(String s) {
        if (s.equals("null")) {
            return null;
        } else {
            return doParse(s);
        }
    }

    public String serialize(Object data) {
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
