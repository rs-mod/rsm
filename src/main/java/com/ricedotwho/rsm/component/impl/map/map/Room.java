package com.ricedotwho.rsm.component.impl.map.map;

import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;

@Getter
public class Room implements Tile {
    @Setter
    private int roofHeight;
    private int bottom;
    private final int x;
    private final int z;
    @Setter
    private RoomData data;
    @Setter
    private int core = 0;
    @Setter
    private boolean isSeparator = false;
    private RoomState state = RoomState.UNDISCOVERED;
    @Setter
    private UniqueRoom uniqueRoom;

    private Room(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Room(int x, int z, RoomData data) {
        this.x = x;
        this.z = z;
        this.roofHeight = RoomUtils.getRoofHeight(this);
        this.bottom = RoomUtils.getRoomBottom(this);
        this.data = data;
    }

    public Room(int x, int z, int roofHeight, int bottom, RoomData data) {
        this.x = x;
        this.z = z;
        this.roofHeight = roofHeight;
        this.bottom = bottom;
        this.data = data;

        ChatUtils.chat("%s bottom: %s top: %s", this.data.name(), this.bottom, this.roofHeight);
    }

    @Override
    public String toString() {
        return "Room" +
                "{" +
                "name=" + this.data.name() +
                ",x=" + x +
                ",z=" + z +
                //",data=" + data.toString() + // ts so long
                ",core=" + core +
                ",isSeparator=" + isSeparator +
                ",state=" + state +
                "}";
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
    public RoomState getState() {
        return state;
    }

    @Override
    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public Colour getColor() {
        if (state == RoomState.UNOPENED) {
            return new Colour(216, 127, 51);
        } else {
            switch (data.type()) {
                case BLOOD:
                    return new Colour(255, 0, 0);
                case CHAMPION:
                    return new Colour(254, 223, 0);
                case ENTRANCE:
                    return new Colour(20, 133, 0);
                case FAIRY:
                    return new Colour(224, 0, 255);
                case PUZZLE:
                    return new Colour(117, 0, 133);
                case RARE:
                    return new Colour(255, 203, 89);
                case TRAP:
                    return new Colour(216, 127, 51);
                default:
                    return new Colour(107, 58, 17);
            }
        }
    }

    public Pair<Integer, Integer> getArrayPosition() {
        return new Pair<>((x - DungeonScanner.startX) / 16, (z - DungeonScanner.startZ) / 16);
    }

    public void addToUnique(int row, int column, String roomName) {
        UniqueRoom unique = DungeonInfo.getUniqueRooms().stream()
                .filter(u -> u.getName().equals(roomName))
                .findFirst()
                .orElse(null);

        if (unique == null) {
            DungeonInfo.getUniqueRooms().add(new UniqueRoom(column, row, this));
        } else {
            unique.addTile(column, row, this);
        }
    }

    public static Room emptyRoom() {
        return new Room(0, 0);
    }

    public void addToUnique(int row, int column) {
        addToUnique(row, column, data.name());
    }

    public Pos getRelativePosition(Pos pos) {
        return RoomUtils.getRelativePosition(pos, this);
    }

    public Pos getRelativePositionFixed(Pos pos) {
        return RoomUtils.getRelativePositionFixed(pos, this);
    }

    public Pos getRelativePosition(BlockPos pos) {
        return RoomUtils.getRelativePosition(pos, this);
    }

    public Pos getRealPosition(Pos pos) {
        return RoomUtils.getRealPosition(pos, this);
    }

    public Pos getRealPositionFixed(Pos pos) {
        return RoomUtils.getRealPositionFixed(pos, this);
    }

    public BlockPos getRealPosition(BlockPos pos) {
        return RoomUtils.getRealPosition(pos, this);
    }
}