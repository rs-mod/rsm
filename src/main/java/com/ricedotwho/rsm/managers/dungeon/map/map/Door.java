package com.ricedotwho.rsm.managers.dungeon.map.map;


import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import lombok.Setter;

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
    private static final Color colorRed =  Color.red.clone();
    private static final Color colorBlue = Color.BLUE.clone();

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
    public Color getColor() {
        if (state == RoomState.UNOPENED) {
            return colorRed;
        } else {
            return switch (type) {
                case ENTRANCE -> colorBlue;
                case WITHER -> opened ? colorRed : colorBlue;
                default -> colorRed;
            };
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
                ",color=" + getColor() + "}";
    }
}