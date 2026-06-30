package com.ricedotwho.rsm.data;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum DungeonClass {
    MAGE("Mage", 3, 2),
    BERSERKER("Berserk", 1, 0),
    TANK("Tank", 3, 1),
    HEALER("Healer", 2, 2),
    ARCHER("Archer", 0, 2),
    NONE("Unknown", -1, 5);

    @Getter
    private static final List<DungeonClass> classes = List.of(MAGE, BERSERKER, TANK, HEALER, ARCHER);

    private final String dClass;
    private final int quadrant;
    private final int priority;
    DungeonClass(String dClass, int quadrant, int priority) {
        this.dClass = dClass;
        this.quadrant = quadrant;
        this.priority = priority;
    }

    public static DungeonClass findClassString(String dClass) {
        return Arrays.stream(DungeonClass.values())
                .filter(type -> dClass.equals(type.getDClass()))
                .findFirst()
                .orElse(DungeonClass.NONE);
    }

    public boolean equals(DungeonClass clazz) {
        return this.getDClass().equalsIgnoreCase(clazz.getDClass());
    }
}
