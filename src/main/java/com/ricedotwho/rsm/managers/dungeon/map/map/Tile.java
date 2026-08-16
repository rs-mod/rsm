package com.ricedotwho.rsm.managers.dungeon.map.map;

import com.ricedotwho.rsm.type.Color;

public interface Tile {
    int getX();
    int getZ();
    RoomState getState();
    void setState(RoomState state);
    Color getColor();
}
