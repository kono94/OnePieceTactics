package net.lwenstrom.tft.backend.core.random;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class DefaultRandomProvider implements RandomProvider {
    private final Random random = new Random();

    @Override
    public <T> void shuffle(List<T> list) {
        Collections.shuffle(list, random);
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
