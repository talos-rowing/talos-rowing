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
package org.nargila.robostroke.param;

/**
 * Data object used as payload for BusEvent.PARAMETER_CHANGE  events
 *
 * @author tshalif
 */
public class ParameterBusEventData {
    private static final String FIELD_SEP = "|";

    /**
     * marks this parameter event as internal - i.e. it is not a replay event
     */
    public final boolean internal;
    public String id;
    public String value;

    /**
     * package private constructor for Parameter event created by ParameterService
     *
     * @param id    parameter identifier
     * @param value parameter value string serialized form
     */
    ParameterBusEventData(String id, String value) {
        this.internal = true;
        this.id = id;
        this.value = value;
    }

    /**
     * public constructor for events generated during a replay of a recorded session
     *
     * @param s ParameterBusEventData string serialized form
     */
    public ParameterBusEventData(String s) {
        internal = false;
        deserialize(s);
    }

    @Override
    public String toString() {
        return id + FIELD_SEP + value;
    }

    private void deserialize(String s) {
        String[] arr = s.split("\\" + FIELD_SEP);

        id = arr[0];
        value = arr[1];
    }
}
