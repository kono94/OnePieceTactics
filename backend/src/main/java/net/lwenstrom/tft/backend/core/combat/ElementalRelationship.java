package net.lwenstrom.tft.backend.core.combat;

import java.util.List;

public record ElementalRelationship(String attacking, List<String> strongAgainst, List<String> resistedBy) {
    public ElementalRelationship {
        strongAgainst = strongAgainst == null ? List.of() : List.copyOf(strongAgainst);
        resistedBy = resistedBy == null ? List.of() : List.copyOf(resistedBy);
    }
}
