package net.lwenstrom.tft.backend.core.model;

import java.util.Map;

public interface GameItem {
    String getId();

    String getName();

    String getDescription();

    Map<String, Integer> getStatBonuses();
}
