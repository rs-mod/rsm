package com.ricedotwho.rsm.component.impl.location;

import lombok.Getter;

import java.util.Arrays;

public enum Floor {
    E("E", 0),
    F1("F1", 1),
    F2("F2", 2),
    F3("F3", 3),
    F4("F4", 4),
    F5("F5", 5),
    F6("F6", 6),
    F7("F7", 7),
    M1("M1", 8),
    M2("M2", 9),
    M3("M3", 10),
    M4("M4", 11),
    M5("M5", 12),
    M6("M6", 13),
    M7("M7", 14),
    // kuudar
    T1("T1", 1),
    T2("T2", 2),
    T3("T3", 3),
    T4("T4", 4),
    T5("T5", 5),
    KUUDRA_ANY("T", 0),

    None(null, -1);

    @Getter
    private final String name;
    @Getter
    private final int index;

    Floor(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static Floor findByName(String name) {
        return Arrays.stream(Floor.values())
                .filter(type -> name.equalsIgnoreCase(type.getName()))
                .findFirst()
                .orElse(Floor.None);
    }
    public static Floor findByIndex(int index) {
        return Arrays.stream(Floor.values())
                .filter(type -> index == type.getIndex())
                .findFirst()
                .orElse(Floor.None);
    }
}
