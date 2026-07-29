package net.lwenstrom.tft.backend.core.model;

import java.util.List;

public record EmergencyDropPayload(String dropId, String playerId, int round, List<String> orbIds) {
    public EmergencyDropPayload {
        orbIds = List.copyOf(orbIds);
    }
}
