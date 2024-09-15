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

package org.nargila.robostroke.way;

import org.nargila.robostroke.common.LocationUtil;
import org.nargila.robostroke.data.DataIdx;

/**
 * resolves GPS location distance diffs
 *
 * @author tshalif
 */
public class DistanceResolverDefault implements DistanceResolver {

    @Override
    public float calcDistance(double[] loc1, double[] loc2) {

        return (float) LocationUtil.distVincenty(loc1[DataIdx.GPS_LAT], loc1[DataIdx.GPS_LONG],
                loc2[DataIdx.GPS_LAT], loc2[DataIdx.GPS_LONG]);

    }

}
