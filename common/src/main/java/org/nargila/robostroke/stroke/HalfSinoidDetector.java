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

package org.nargila.robostroke.stroke;

import org.nargila.robostroke.common.filter.LowpassFilter;

/**
 * Detects half sinoid event with dynamic amplitude change adaptation
 * @author tshalif
 *
 */
class HalfSinoidDetector {
    private static final float DEFAULT_AMPLITUDE_CHANGE_DAMP_FACTOR = .5f;
    private static final float DEFAULT_AMPLITUDE_CHANGE_ACCEPT_FACTOR = .5f;
    private static final float DEFAULT_MIN_AMPLITUDE = .02f;
        

    enum State {
        NONE,
            ENTER,
            TESHOLD_PASS,
            VALID_EXIT,
            INVALID_EXIT            
            }
        
    private State state = State.NONE;
        
    /**
     * Identifies either this object is to detect above zero 'UP' or
     * below zero 'DOWN' half of the stroke acceleration sinoid
     */
    enum Dir {
        UP,
            DOWN
            }
        
    private final Dir dir;
        
    private float amplitudeChangeAcceptFactor;
    private float amplitudeDampedValue;
    private float minAmplitude;
    float maxVal;
    private final LowpassFilter amplitudeChangeDamper = new LowpassFilter();
    private boolean wasInside;
        
    HalfSinoidDetector(Dir dir) {
        this(dir, DEFAULT_MIN_AMPLITUDE, DEFAULT_AMPLITUDE_CHANGE_DAMP_FACTOR, DEFAULT_AMPLITUDE_CHANGE_ACCEPT_FACTOR);
    }
        
    HalfSinoidDetector(Dir dir, float minAmplitude) {
        this(dir, minAmplitude, DEFAULT_AMPLITUDE_CHANGE_DAMP_FACTOR, DEFAULT_AMPLITUDE_CHANGE_ACCEPT_FACTOR);
    }
        
    HalfSinoidDetector(Dir dir, float minAmplitude, float changeDampFactor, float changeAcceptFactor) {
        this.dir = dir;
                
        setMinAmplitude(minAmplitude);
        setAmplitudeChangeDamperFactor(changeDampFactor);
        setAmplitudeChangeAcceptFactor(changeAcceptFactor);
    }

    public float getAmplitudeChangeAcceptFactor() {
        return amplitudeChangeAcceptFactor;
    }

    public void setAmplitudeChangeAcceptFactor(float amplitudeChangeAcceptFactor) {
        this.amplitudeChangeAcceptFactor = amplitudeChangeAcceptFactor;
    }

    public float getMinAmplitude() {
        return minAmplitude;
    }

    public void setMinAmplitude(float minAmplitude) {
        this.minAmplitude = minAmplitude;
    }

    public float getAmplitudeChangeDamperFactor() {
        return amplitudeChangeDamper.getFilteringFactor();
    }

    public void setAmplitudeChangeDamperFactor(float factor) {
        amplitudeChangeDamper.setFilteringFactor(factor);
    }

    /**
     * checks either the acceleration value is inside the jurisdiction of this object
     * @param v acceleration value
     * @return true if insde, false if not
     */
    private boolean checkInside(float v) {
        switch (dir) {
        case DOWN:
            if (v < 0) {
                return true;
            }
            break;
        case UP:
            if (v > 0) {
                return true;
            }
            break;                  
        }
        return false;
    }
        
    /**
     * add acceleration value and return true if a stroke was detected
     * @param v stroke acceleration value
     * @return true if this added acceleration value <code>v</code> causes a detection of a stroke
     */
    State add(float v) {
        State retval = State.NONE;
                
        if (checkInside(v)) {
                        
            if (!wasInside) {
                maxVal = 0;
            }
                        
            maxVal = Math.max(Math.abs(v), maxVal);
            wasInside = true;
                        
            switch (state) {
            case NONE:
                state = State.ENTER;
                /* no break */
            case ENTER:
                if (maxVal > minAmplitude && maxVal > amplitudeDampedValue * amplitudeChangeAcceptFactor) {
                    state = State.TESHOLD_PASS;
                    retval = State.TESHOLD_PASS;
                }
                break;
            }
                        
        } else if (wasInside) { 
                        
            amplitudeDampedValue = amplitudeChangeDamper.filter(new float[]{maxVal})[0];
                        
            switch (state) {
            case TESHOLD_PASS:
                retval = State.VALID_EXIT;
                break;
            default:
                retval = State.INVALID_EXIT;
                break;
            }
                        
            state = State.NONE;

            wasInside = false;
                        
                                                
        }
                                
        return retval;
    }

}
