package com.ricedotwho.rsm.component.impl.map.map;

public enum DoorType {
    BLOOD(18),
    ENTRANCE(30),
    NORMAL(74, 82, 66, 62, 85, 63), // Yellow, Fairy, Puzzle, Trap, Unopened doors render as normal doors
    WITHER(119);

    private final int[] mapColors;

    DoorType(int... mapColors) {
        this.mapColors = mapColors;
    }

    public static DoorType fromMapColor(int color) {
        for (DoorType doorType : DoorType.values()) {
            for (int mapColor : doorType.mapColors) {
                if (mapColor == color) {
                    return doorType;
                }
            }
        }
        return null;
    }
}
