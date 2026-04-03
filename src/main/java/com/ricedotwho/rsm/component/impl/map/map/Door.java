package com.ricedotwho.rsm.component.impl.map.map;


import com.ricedotwho.rsm.data.Colour;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
public class Door implements Tile {
    private final int x;
    private final int z;
    @Setter
    private DoorType type;
    @Setter
    private boolean opened;
    @Setter
    private RoomState state;
    private static final Colour colourRed = new Colour(Color.red);
    private static final Colour colourBlue = new Colour(Color.BLUE);

    public Door(int x, int z, DoorType type) {
        this.x = x;
        this.z = z;
        this.type = type;
        this.opened = false;
        this.state = RoomState.UNDISCOVERED;
    }

    public Door(Door door) {
        this.x = door.getX();
        this.z = door.getZ();
        this.type = door.getType();
        this.opened = door.isOpened();
        this.state = door.getState();
    }

    @Override
    public Colour getColor() {
        if (state == RoomState.UNOPENED) {
            return colourRed;
        } else {
            switch (type) {
                case ENTRANCE:
                    return colourBlue;
                case WITHER:
                    return opened ? colourRed : colourBlue;
                default:
                    return colourRed;
            }
        }
    }
    @Override
    public String toString() {
        return "Door{" +
                "x=" + x +
                ",z=" + z +
                ",type=" + type +
                ",opened=" + opened +
                ",state=" + state +
                ",colour=" + getColor() + "}";
    }
}