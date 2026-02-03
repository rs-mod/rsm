package com.ricedotwho.rsm.component.impl.map.map;

import net.minecraft.core.Direction;

public enum RoomRotation {
    TOPLEFT,
    TOPRIGHT,
    BOTLEFT,
    BOTRIGHT,
    UNKNOWN;

    public Direction toDir() {
        switch (this) {
            case TOPLEFT -> {
                return Direction.NORTH;
            }
            case TOPRIGHT -> {
                return Direction.SOUTH;
            }
            case BOTLEFT -> {
                return Direction.WEST;
            }
            case BOTRIGHT -> {
                return Direction.EAST;
            }
        }
        return null;
    }
}
