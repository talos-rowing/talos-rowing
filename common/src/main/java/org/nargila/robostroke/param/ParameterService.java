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

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.data.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Parameter registration/notification service
 *
 * @author tshalif
 */
public class ParameterService {

    private static final Logger logger = LoggerFactory.getLogger(ParameterService.class);

    private final RoboStrokeEventBus eventBus;

    private final HashMap<String, HashSet<ParameterChangeListener>> listeners = new HashMap<String, HashSet<ParameterChangeListener>>();

    private final HashMap<String, Parameter> paramMap = new HashMap<String, Parameter>();

    private boolean recursionOn;

    public ParameterService(RoboStrokeEventBus bus) {
        this.eventBus = bus;

        addListener("*", new ParameterChangeListener() {

            @Override
            public void onParameterChanged(Parameter param) {
                eventBus.fireEvent(DataRecord.Type.PARAMETER_CHANGE, new ParameterBusEventData(param.getId(), param.convertToString()));
            }
        });
        /**
         * register a bus listener, to enable replaying of parameter value changes
         */
        eventBus.addBusListener(new BusEventListener() {

            @Override
            public void onBusEvent(DataRecord event) {
                switch (event.type) {
                    case PARAMETER_CHANGE:
                        ParameterBusEventData data = (ParameterBusEventData) event.data;

                        if (!data.internal) {
                            setParam(data.id, data.value);
                        }
                }

            }
        });
    }


    public synchronized void addListener(ParameterListenerRegistration... value) {
        for (ParameterListenerRegistration lr : value) {
            addListener(lr.getParamId(), lr.getListener());
        }
    }

    public synchronized void removeListener(ParameterListenerRegistration... value) {
        for (ParameterListenerRegistration lr : value) {
            removeListener(lr.getParamId(), lr.getListener());
        }
    }

    public void addListeners(ParameterListenerOwner listenersOwner) {
        addListener(listenersOwner.getListenerRegistrations());
    }

    public void removeListeners(ParameterListenerOwner listenersOwner) {
        removeListener(listenersOwner.getListenerRegistrations());
    }

    public synchronized void removeListener(String paramId, ParameterChangeListener listener) {

        if (!paramId.equals("*")) {
            getParam(paramId); // throws exception if paramId not already registered
        }

        HashSet<ParameterChangeListener> listenerSet = listeners.get(paramId);

        if (listenerSet == null || !listenerSet.remove(listener)) {
            throw new IllegalArgumentException("trying to remove unregistered listener for param " + paramId);
        }

    }

    public synchronized void addListener(String paramId, ParameterChangeListener listener) {
        if (!paramId.equals("*")) {
            getParam(paramId); // throws exception if paramId not already registered
        }

        HashSet<ParameterChangeListener> listenerSet = listeners.get(paramId);

        if (listenerSet == null) {
            listenerSet = new HashSet<ParameterChangeListener>();
            listeners.put(paramId, listenerSet);
        }

        listenerSet.add(listener);
    }

    public synchronized void registerParam(Parameter... param) {

        for (Parameter p : param) {
            final String id = p.getId();

            if (paramMap.containsKey(id)) {
                throw new IllegalArgumentException("parameter " + id + " is already registered");
            }
        }

        for (Parameter p : param) {
            p.parameterService = this;
            paramMap.put(p.getId(), p);
        }
    }

    public synchronized void setParam(Parameter param, Object value) {

        // check either param is registered
        if (getParam(param.getId()) != param) {
            throw new IllegalArgumentException(String.format("parameter provided with id %s is not the same as the registered one"));
        }

        try {
            if (recursionOn) {
                throw new IllegalStateException("recursion detected - parameter listeners are not allowed to modify parameters from within same thread");
            }

            recursionOn = true;

            if (param.setParameterValue(value)) {
                onParamChanged(param);
            }
        } finally {
            recursionOn = false;
        }
    }


    private void onParamChanged(Parameter param) {

        HashSet<ParameterChangeListener> paramChangeListeners = listeners.get(param.getId());

        HashSet<ParameterChangeListener> allListeners = new HashSet<ParameterChangeListener>();

        if (paramChangeListeners == null) {
            logger.warn("no listeners registered for param {}", param.getId());
        } else {
            allListeners.addAll(paramChangeListeners); // add param-specific listeners before "*"
        }

        allListeners.addAll(listeners.get("*"));

        for (ParameterChangeListener listener : allListeners) {
            listener.onParameterChanged(param);
        }
    }

    public synchronized void setParam(String id, Object value) {
        Parameter param = getParam(id);

        Object val;
        if (value instanceof String) {
            val = param.convertFromString((String) value);
        } else {
            val = value;
        }

        setParam(param, val);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String id) {
        return (T) getParam(id).getValue();
    }

    public Map<String, Parameter> getParamMap() {
        return Collections.unmodifiableMap(paramMap);
    }

    public synchronized Parameter getParam(String id) {
        Parameter param = paramMap.get(id);
        if (param == null) {
            throw new IllegalArgumentException("param " + id + " is not registered");
        }

        return param;
    }
}
