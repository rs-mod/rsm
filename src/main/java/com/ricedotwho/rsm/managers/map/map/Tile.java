package com.ricedotwho.rsm.managers.map.map;

import com.ricedotwho.rsm.data.Colour;

public interface Tile {
    int getX();
    int getZ();
    RoomState getState();
    void setState(RoomState state);
    Colour getColor();
}
