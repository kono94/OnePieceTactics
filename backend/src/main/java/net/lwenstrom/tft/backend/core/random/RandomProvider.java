package net.lwenstrom.tft.backend.core.random;

import java.util.List;
import java.util.Random;

public interface RandomProvider {
    <T> void shuffle(List<T> list);

    int nextInt(int bound);

    double nextDouble();

    Random getRandom();
}
