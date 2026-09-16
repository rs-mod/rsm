package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.type.Vec2i;

public enum RoomRotation {
    NORTH(new Vec2i(15, 15), new Vec2i(0, 7)), // Bottom right
    SOUTH(new Vec2i(-15, -15), new Vec2i(0, -7)), // Top left
    EAST(new Vec2i(15, -15), new Vec2i(7, 0)), // Top right
    WEST(new Vec2i(-15, 15), new Vec2i(-7, 0)), // Bottom left
    UNKNOWN(new Vec2i(), new Vec2i());



    public final Vec2i normal;
    public final Vec2i fourByOne;

    RoomRotation(Vec2i normal, Vec2i fourByOne) {
        this.normal = normal;
        this.fourByOne = fourByOne;
    }

    public static RoomRotation[] values = { NORTH, WEST, SOUTH, EAST};
}
