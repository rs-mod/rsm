package com.ricedotwho.rsm.location;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public enum Floor {
    E("E", 0, 0),
    F1("F1", 1, 1),
    F2("F2", 2 ,2),
    F3("F3", 3, 3),
    F4("F4", 4, 4),
    F5("F5", 5, 5),
    F6("F6", 6, 6),
    F7("F7", 7, 7),
    M1("M1", 8, 1),
    M2("M2", 9, 2),
    M3("M3", 10, 3),
    M4("M4", 11, 4),
    M5("M5", 12, 5),
    M6("M6", 13, 6),
    M7("M7", 14, 7),
    // kuudar
    T1("T1", 1, 0),
    T2("T2", 2, 0),
    T3("T3", 3, 0),
    T4("T4", 4, 0),
    T5("T5", 5, 0),
    KUUDRA_ANY("T", 0, 0),

    NONE(null, -1, 0);

    @Getter
    private final String name;
    @Getter
    private final int index;
    @Getter
    private final int number;

    Floor(String name, int index, int number) {
        this.name = name;
        this.index = index;
        this.number = number;
    }

    public static Floor findByName(String name) {
        return Arrays.stream(Floor.values())
                .filter(type -> name.equalsIgnoreCase(type.getName()))
                .findFirst()
                .orElse(Floor.NONE);
    }
    public static Floor findByIndex(int index) {
        return Arrays.stream(Floor.values())
                .filter(type -> index == type.getIndex())
                .findFirst()
                .orElse(Floor.NONE);
    }

    public static List<Floor> dungeonValues() {
        return List.of(F1, F2, F3, F4, F5, F6, F7, M1, M2, M3, M4, M5, M6, M7);
    }

    public boolean isDungeons() {
        return this.getName() != null && !this.getName().contains("T");
    }
}
