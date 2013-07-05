package org.nargila.robostroke.data;

public class SystemClockProvider implements ClockProvider {

    private long systemTimeOffset;
    private long stopTime;
    private boolean stopped = true;
    
    public SystemClockProvider() {
    }

    @Override
    public long getTime() {
        return stopped ? stopTime : System.currentTimeMillis() - systemTimeOffset;
    }

    @Override
    public void run() {
        if (!stopped) {
            throw new IllegalStateException("timer is already running");
        }
        
        reset(stopTime);
        
        stopped = false;
    }

    @Override
    public void stop() {
        if (stopped) {
            throw new IllegalStateException("timer is not running");
        }
        
        stopTime = getTime();
        
        stopped = true;
    }

    @Override
    public void reset(long initialTime) {
        systemTimeOffset = System.currentTimeMillis() - initialTime;
    }

}
