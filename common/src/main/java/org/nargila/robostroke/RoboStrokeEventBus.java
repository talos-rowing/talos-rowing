/*
 * Copyright (c) 2011 Tal Shalif
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
package org.nargila.robostroke;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.BusEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple communication bus enabling communication between various components 
 * @author tshalif
 *
 */
public class RoboStrokeEventBus extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(RoboStrokeEventBus.class);
	
	private static final int DEBUG_QUEUE_OVERFLOW_SIZE = 50;
	private static final int DEBUG_QUEUE_WARN_SIZE = 20;
	
	private final LinkedBlockingQueue<BusEvent> eventQueue = 
		new LinkedBlockingQueue<BusEvent>();
	
	private final LinkedList<BusEventListener> listeners = new LinkedList<BusEventListener>();

	private boolean shutdown;

	public RoboStrokeEventBus() {
		super("RoboStrokeEventBus thread");
		setDaemon(true);
		start();
	}
	
	public void shutdown() {
		shutdown = true;
		interrupt();
	}
	
	@Override
	public void run() {
		while (!shutdown) {
			try {
				BusEvent event = eventQueue.take();

				if (shutdown) {
					break;
				}
				
				synchronized (listeners) {
					for (BusEventListener listener: listeners) {
						if (shutdown) {
							break;
						}
						listener.onBusEvent(event);
					}
				}
			} catch (InterruptedException e) {
				if (!shutdown) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	/**
	 * add stroke rate listener
	 * @param listener listener object
	 */
	public void addBusListener(BusEventListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * remove stroke rate listener
	 * @param listener listener object
	 */
	public void removeBusListener(BusEventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void fireEvent(BusEvent event) {
		if (shutdown) {
			return;
		}

		if (eventQueue.size() > DEBUG_QUEUE_OVERFLOW_SIZE) {
			logger.error("event bus overflow: event queue exceeds {} items", DEBUG_QUEUE_OVERFLOW_SIZE);
		} else if (eventQueue.size() > DEBUG_QUEUE_WARN_SIZE) {
			logger.warn("event bus size warning: event queue exceeds {} items", DEBUG_QUEUE_WARN_SIZE);
		}

		try {
			logger.debug("fire event {}", event);
			eventQueue.put(event);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fireEvent(Type type, Object data) {
		fireEvent(new BusEvent(type, TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()), data));		
	}
	
	public void fireEvent(Type type, long timestamp, Object ... data) {
		fireEvent(new BusEvent(type, timestamp, data));		
	}
	
	public void fireEvent(Type type, long timestamp, Object data) {
		fireEvent(new BusEvent(type, timestamp, data));		
	}
}