package com.ricedotwho.rsm.managers.dungeon.map.map;

import com.ricedotwho.rsm.type.Color;

public class Unknown implements Tile {
    private final int x;
    private final int z;
    private final Color color = new Color(0, 0, 0, 0);
    private RoomState state = RoomState.UNDISCOVERED;

    public Unknown(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public RoomState getState() {
        return state;
    }

    @Override
    public void setState(RoomState state) {
        this.state = state;
    }
}
