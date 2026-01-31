package com.ricedotwho.rsm.component.impl.map.map;

public enum RoomType {
    BLOOD(18),
    CHAMPION(74),
    ENTRANCE(30),
    FAIRY(82),
    NORMAL(63, 85),
    PUZZLE(66),
    RARE(-1),
    TRAP(62),
    BOSS(0);

    private final int[] mapColors;

    RoomType(int... mapColors) {
        this.mapColors = mapColors;
    }

    public static RoomType fromMapColor(int color) {
        for (RoomType roomType : RoomType.values()) {
            for (int mapColor : roomType.mapColors) {
                if (mapColor == color) {
                    return roomType;
                }
            }
        }
        return null;
    }
}
