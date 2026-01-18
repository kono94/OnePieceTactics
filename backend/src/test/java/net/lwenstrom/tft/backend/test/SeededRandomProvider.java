package net.lwenstrom.tft.backend.test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.lwenstrom.tft.backend.core.random.RandomProvider;

public class SeededRandomProvider implements RandomProvider {
    private final Random random;

    public SeededRandomProvider(long seed) {
        this.random = new Random(seed);
    }

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
