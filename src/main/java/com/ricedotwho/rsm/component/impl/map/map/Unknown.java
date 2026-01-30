package com.ricedotwho.rsm.component.impl.map.map;

import com.ricedotwho.rsm.data.Colour;

public class Unknown implements Tile {
    private final int x;
    private final int z;
    private final Colour color = new Colour(0, 0, 0, 0);
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
    public Colour getColor() {
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
