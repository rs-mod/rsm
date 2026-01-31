package com.ricedotwho.rsm.data;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DungeonClass {
    MAGE("Mage"),
    BERSERKER("Berserk"),
    TANK("Tank"),
    HEALER("Healer"),
    ARCHER("Archer"),
    NONE("EMPTY");

    private final String dClass;

    DungeonClass(String dClass) {
        this.dClass = dClass;
    }

    public static DungeonClass findClassString(String dClass) {
        return Arrays.stream(DungeonClass.values())
                .filter(type -> dClass.equals(type.getDClass()))
                .findFirst()
                .orElse(DungeonClass.NONE);
    }
}
