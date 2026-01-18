package net.lwenstrom.tft.backend.test;

import net.lwenstrom.tft.backend.core.time.Clock;

public class TestClock implements Clock {
    private long currentTime = 0;

    @Override
    public long currentTimeMillis() {
        return currentTime;
    }

    public void advance(long ms) {
        currentTime += ms;
    }

    public void setTime(long time) {
        currentTime = time;
    }
}
