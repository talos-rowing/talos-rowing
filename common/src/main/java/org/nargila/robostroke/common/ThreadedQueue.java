package org.nargila.robostroke.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class ThreadedQueue<T> {

    private static final Logger logger = LoggerFactory.getLogger(ThreadedQueue.class);

    private final ArrayBlockingQueue<T> recordQueue;

    private boolean enabled;

    private final Thread thread;

    private final String name;

    private boolean stop;

    protected ThreadedQueue(String name, int capacity) {

        recordQueue = new ArrayBlockingQueue<T>(capacity);

        this.name = name;

        synchronized (this) {
            thread = new Thread("Queue " + name) {
                {
                    setDaemon(true);
                }

                public void run() {
                    runImpl();
                }
            };

            thread.start();

            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public synchronized void setEnabled(boolean enable) {

        enabled = enable && !stop;

        if (this.enabled != enable) {
            this.enabled = enable;

            recordQueue.clear();
        }
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public void put(T o) {

        if (enabled) {

            if (o == null) {
                throw new IllegalArgumentException("null queue object not allowed");
            }

            while (!recordQueue.offer(o)) {

                logger.warn("queue {} overflow", name);

                recordQueue.poll();
            }
        }
    }


    private void runImpl() {

        synchronized (this) {
            notifyAll();
        }

        while (!stop) {
            try {
                if (enabled) {
                    T o = recordQueue.poll(10, TimeUnit.MILLISECONDS);

                    if (o != null) {
                        handleItem(o);
                    }
                } else {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    protected abstract void handleItem(T o);

    public synchronized void stop() {

        stop = true;

        setEnabled(false);

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
