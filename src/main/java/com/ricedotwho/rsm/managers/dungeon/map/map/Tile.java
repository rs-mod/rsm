package com.ricedotwho.rsm.managers.dungeon.map.map;

import com.ricedotwho.rsm.type.Colour;

public interface Tile {
    int getX();
    int getZ();
    RoomState getState();
    void setState(RoomState state);
    Colour getColor();
}
