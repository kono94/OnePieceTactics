package net.lwenstrom.tft.backend.core.time;

import org.springframework.stereotype.Component;

@Component
public class SystemClock implements Clock {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
