/*
 * Copyright (c) 2007 Wayne Meissner
 *
 * This file is part of gstreamer-java.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.robostroke.common;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


/**
 * A representation of time in the gstreamer framework.
 */
public final class ClockTime implements Comparable<ClockTime>, Serializable {

    private static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // Static variables
    //
    public final static ClockTime NONE = new ClockTime(-1, TimeUnit.NANOSECONDS);
    public final static ClockTime ZERO = new ClockTime(0, TimeUnit.NANOSECONDS);

    //--------------------------------------------------------------------------
    // Instance variables
    //
    private long value; // not final because of GWT serialization

    /**
     * Creates a new instance of Time
     *
     * @param time  the length of time this object represents.
     * @param units the units <tt>time</tt> is expressed in.
     */
    private ClockTime(long time, TimeUnit units) {
        setValue(units.toNanos(time));
    }

    /**
     * Creates a new instance from System.currentTimeMillis()
     */
    public ClockTime() {
        this(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }


    private void setValue(long value) {
        this.value = value;
    }

    /**
     * Creates a new ClockTime object for a microsecond value.
     *
     * @param microseconds The microsecond value to represent.
     * @return The new ClockTime object.
     */
    public static ClockTime fromMicros(long microseconds) {
        return valueOf(microseconds, TimeUnit.MICROSECONDS);
    }

    /**
     * Creates a new ClockTime object for a timestamp value.
     *
     * @param timestamp timestamp inf spu format of HH:MM:SS,mmm
     * @return The new ClockTime object.
     */
    public static ClockTime fromString(String timestamp) {
        return valueOf(timestamp);
    }

    /**
     * Creates a new ClockTime object for a millisecond value.
     *
     * @param milliseconds The millisecond value to represent.
     * @return The new ClockTime object.
     */
    public static ClockTime fromMillis(long milliseconds) {
        return valueOf(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new ClockTime object for a nanosecond value.
     *
     * @param nanoseconds The nanosecond value to represent.
     * @return The new ClockTime object.
     */
    public static ClockTime fromNanos(long nanoseconds) {
        return valueOf(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * Creates a new ClockTime object for a second value.
     *
     * @param seconds The second value to represent.
     * @return The new ClockTime object.
     */
    public static ClockTime fromSeconds(long seconds) {
        return valueOf(seconds, TimeUnit.SECONDS);
    }

    /**
     * Returns a new ClockTime object that represents the <tt>time</tt> value.
     *
     * @param time  the length of time this object represents, in value.
     * @param units the units <tt>time</tt> is expressed in.
     * @return The new ClockTime object.
     */
    public static ClockTime valueOf(long time, TimeUnit units) {
        long nanoseconds = units.toNanos(time);
        if (nanoseconds == -1L) {
            return NONE;
        } else if (nanoseconds == 0L) {
            return ZERO;
        }
        return new ClockTime(time, units);
    }

    /**
     * Returns a new ClockTime object that represents the <tt>timestamp</tt> value.
     *
     * @param timestamp timestamp inf spu format of HH:MM:SS,mmm
     * @return The new ClockTime object.
     */
    public static ClockTime valueOf(String timestamp) {
        //private static final Pattern SPU_TIMESTAMP_PATTERN = Pattern.compile("^([0-9]+):([0-9][0-9]):([0-9][0-9]),([0-9][0-9][0-9])$");

        String[] ss = timestamp.split(",");

        String[] tt = ss[0].split(":");

        int hours = new Integer(tt[0]);
        int minutes = new Integer(tt[1]);
        int seconds = new Integer(tt[2]);
        int millis = new Integer(ss[1]);

        if (minutes > 59 || minutes < 0 || seconds > 59 || seconds < 0 || millis > 999 || millis < 0) {
            throw new IllegalArgumentException("bad timestamp format " + timestamp);
        }


        long ms =
            TimeUnit.HOURS.toMillis(hours) +
            TimeUnit.MINUTES.toMillis(minutes) +
            TimeUnit.SECONDS.toMillis(seconds) + millis;

        return valueOf(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the hours component of the total time.
     *
     * @return The hours component of the total time.
     */
    public long getHours() {
        return (toSeconds() / 3600) % 24;
    }

    /**
     * Get the minutes component of the total time.
     *
     * @return The minutes component of the total time.
     */
    public long getMinutes() {
        return (toSeconds() / 60) % 60;
    }

    /**
     * Get the seconds component of the total time.
     *
     * @return The seconds component of the total time.
     */
    public long getSeconds() {
        return toSeconds() % 60;
    }

    /**
     * Get the nanosecond component of the total time.
     *
     * @return The value component of the total time.
     */
    public long getNanoSeconds() {
        return value % TimeUnit.SECONDS.toNanos(1);
    }

    /**
     * Converts this ClockTime to a time value of <tt>unit</tt> units.
     *
     * @param unit the {@link TimeUnit} to convertTo this time to.
     * @return the total time represented by this ClockTime.
     * @see TimeUnit#convert
     */
    public long convertTo(TimeUnit unit) {
        return unit.convert(value, TimeUnit.NANOSECONDS);
    }

    /**
     * Gets the total number of microseconds represented by this {@code ClockTime}.
     * <p> This is a convenience wrapper, equivalent to:
     * <p> {@code convertTo(TimeUnit.MICROSECONDS) }
     *
     * @return The total microseconds represented by this {@code ClockTime}.
     */
    public long toMicros() {
        return convertTo(TimeUnit.MICROSECONDS);
    }

    /**
     * Gets the total number of milliseconds represented by this {@code ClockTime}.
     * <p> This is a convenience wrapper, equivalent to:
     * <p> {@code convertTo(TimeUnit.MILLISECONDS) }
     *
     * @return The total milliseconds represented by this {@code ClockTime}.
     */
    public long toMillis() {
        return convertTo(TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the total number of value represented by this {@code ClockTime}.
     * <p> This is a convenience wrapper, equivalent to:
     * <p> {@code convertTo(TimeUnit.NANOSECONDS) }
     *
     * @return The total value represented by this {@code ClockTime}.
     */
    public long toNanos() {
        return convertTo(TimeUnit.NANOSECONDS);
    }

    /**
     * Gets the total number of seconds represented by this {@code ClockTime}.
     * <p> This is a convenience wrapper, equivalent to:
     * <p> {@code convertTo(TimeUnit.SECONDS) }
     *
     * @return The total seconds represented by this {@code ClockTime}.
     */
    public long toSeconds() {
        return convertTo(TimeUnit.SECONDS);
    }

    /**
     * Determines if this ClockTime represents a valid time value.
     *
     * @return true if valid, else false
     */
    public boolean isValid() {
        return value != NONE.value;
    }

    /**
     * Returns a {@code String} representation of this {@code ClockTime}.
     *
     * @return a string representation of this {@code ClockTime}
     */
    @Override
    public String toString() {
        long hours = getHours();
        long minutes = getMinutes();
        long seconds = getSeconds();
        long millis = getNanoSeconds() / 1000000;

        return new StringBuilder()
                .append(hours > 9 ? "" : "0").append(getHours()).append(":")
                .append(minutes > 9 ? "" : "0").append(minutes).append(":")
                .append(seconds > 9 ? "" : "0").append(seconds).append(",")
                .append(millis > 99 ? "" : (millis > 9 ? "0" : "00")).append(millis)
                .toString();
    }

    /**
     * Compares this {@code ClockTime} to the specified object.
     * <p> The result is {@code true} if and only if the argument is not
     * {@code null} and is a {@code ClockTime} object equivalent to this
     * {@code ClockTime}
     *
     * @param obj
     * @return <tt>true</tt> if the specified object is equivalent to this
     * {@code ClockTime}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClockTime && ((ClockTime) obj).value == value;
    }

    /**
     * Returns a hash code for this {@code ClockTime}.
     *
     * @return a hash code value for this ClockTime.
     * @see java.lang.Long#hashCode
     */
    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }


    /**
     * Compares this ClockTime to another.
     *
     * @param time the other ClockTime to compare to.
     * @return {@code 0} if this {@code ClockTime} is equal to <tt>time</tt>.
     * A value less than zero if this {@code ClockTime} is numerically less than
     * <tt>time</tt>.
     * A value greater than zero if this {@code ClockTime} is numerically
     * greater than <tt>time</tt>.
     */
    public int compareTo(ClockTime time) {
        if (value < time.value) {
            return -1;
        } else if (value > time.value) {
            return 1;
        }
        return 0;
    }
}
