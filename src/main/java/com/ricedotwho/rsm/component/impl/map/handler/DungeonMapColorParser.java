package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.impl.map.map.*;
import com.ricedotwho.rsm.component.impl.map.utils.MapUtils;
import com.ricedotwho.rsm.data.Pair;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DungeonMapColorParser {
    private byte[] centerColors = new byte[121];
    private byte[] sideColors = new byte[121];
    private Tile[] cachedTiles = new Tile[121];

    private int halfRoom = -1;
    private int halfTile = -1;
    private int quarterRoom = -1;
    private int startX = -1;
    private int startY = -1;

    private final Pair<Integer, Integer>[] directions = new Pair[] {
            new Pair<>(0, -1), // north
            new Pair<>(0, 1), // south
            new Pair<>(-1, 0), // west
            new Pair<>(1, 0), // east
    };

    public void calibrate() {
        halfRoom = MapUtils.mapRoomSize / 2;
        halfTile = halfRoom + 2;
        quarterRoom = halfRoom / 2;
        startX = MapUtils.startCorner.getFirst() + halfRoom;
        startY = MapUtils.startCorner.getSecond() + halfRoom;

        centerColors = new byte[121];
        sideColors = new byte[121];
        cachedTiles = new Tile[121];
    }

    public void updateMap(MapItemSavedData mapData) {
        cachedTiles = new Tile[121];

        for (int y = 0; y <= 10; y++) {
            for (int x = 0; x <= 10; x++) {
                int mapX = startX + x * halfTile;
                int mapY = startY + y * halfTile;

                if (mapX >= 128 || mapY >= 128) continue;

                centerColors[y * 11 + x] = mapData.colors[mapY * 128 + mapX];

                int sideIndex;
                if (x % 2 == 0 && y % 2 == 0) {
                    int topX = mapX - halfRoom;
                    int topY = mapY - halfRoom;
                    sideIndex = topY * 128 + topX;
                } else {
                    boolean horizontal = y % 2 == 1;
                    if (horizontal) {
                        sideIndex = mapY * 128 + mapX - 4;
                    } else {
                        sideIndex = (mapY - 4) * 128 + mapX;
                    }
                }

                sideColors[y * 11 + x] = mapData.colors[sideIndex];
            }
        }
    }

    public Tile getTile(int arrayX, int arrayY) {
        int index = arrayY * 11 + arrayX;
        if (index < 0 || index >= cachedTiles.length) return null;
        Tile cached = cachedTiles[index];
        if (cached == null) {
            int xPos = DungeonScanner.startX + arrayX * (DungeonScanner.roomSize >> 1);
            int zPos = DungeonScanner.startZ + arrayY * (DungeonScanner.roomSize >> 1);
            cachedTiles[index] = scanTile(arrayX, arrayY, xPos, zPos);
        }
        return cachedTiles[index] != null ? cachedTiles[index] : new Unknown(0, 0);
    }

    public List<Room> getConnected(int arrayX, int arrayY) {
        Tile tile = getTile(arrayX, arrayY);
        if (!(tile instanceof Room)) return new ArrayList<>();
        List<Room> connected = new ArrayList<>();
        ArrayDeque<Room> queue = new ArrayDeque<>();
        queue.add((Room) tile);
        while (!queue.isEmpty()) {
            Room current = queue.removeFirst();
            connected.add(current);
            for (Pair<Integer, Integer> facing : directions) {
                Tile neighbor = getTile(current.getX() + facing.getFirst(), current.getZ() + facing.getSecond());
                if (neighbor instanceof Room) {
                    queue.add((Room) neighbor);
                }
            }
        }
        return connected;
    }

    private Tile scanTile(int arrayX, int arrayY, int worldX, int worldZ) {
        int centerColor = centerColors[arrayY * 11 + arrayX] & 0xFF;
        int sideColor = sideColors[arrayY * 11 + arrayX] & 0xFF;

        if (centerColor == 0) return new Unknown(worldX, worldZ);

        if (arrayX % 2 == 0 && arrayY % 2 == 0) {
            RoomType type = RoomType.fromMapColor(sideColor);
            if (type == null) return new Unknown(worldX, worldZ);
            RoomData roomData = RoomData.createUnknown(type);
            Room room = new Room(worldX, worldZ, roomData);
            RoomState state = getRoomState(centerColor, type, room);
            room.setState(state);
            return room;
        } else {
            if (sideColor == 0) {
                DoorType type = DoorType.fromMapColor(centerColor);
                if (type == null) return new Unknown(worldX, worldZ);
                Door door = new Door(worldX, worldZ, type);
                door.setState(centerColor == 85 ? RoomState.UNOPENED : RoomState.DISCOVERED);
                return door;
            } else {
                RoomType type = RoomType.fromMapColor(sideColor);
                if (type == null) return new Unknown(worldX, worldZ);
                Room room = new Room(worldX, worldZ, RoomData.createUnknown(type));
                room.setState(RoomState.DISCOVERED);
                room.setSeparator(true);
                return room;
            }
        }
    }

    public RoomState getRoomState(int centerColor, RoomType type, Room room) {
        RoomState state;
        if (centerColor == 18) {
            if (type == RoomType.BLOOD) {
                state = RoomState.DISCOVERED;
            } else if (type == RoomType.PUZZLE) {
                state = RoomState.FAILED;
            } else {
                state = room.getState();
            }
        } else if (centerColor == 30) {
            if (type == RoomType.ENTRANCE) {
                state = RoomState.DISCOVERED;
            } else {
                state = RoomState.GREEN;
            }
        } else if (centerColor == 34) {
            state = RoomState.CLEARED;
        } else if (centerColor == 85 || centerColor == 119) {
            state = RoomState.UNOPENED;
        } else {
            state = RoomState.DISCOVERED;
        }
        return state;
    }

    public void updateRoomState(Room room) {
        if (room == null) return;
        assert Minecraft.getInstance().level != null;
        if (!Minecraft.getInstance().level.isLoaded(new BlockPos(room.getX(), 67, room.getZ()))) return; // six sevennn

        int index = room.getZ() * 11 + room.getX();
        if (centerColors.length < index || index < 0) {
            System.out.println(centerColors.length + " < " + index);
            return;
        }
        int centerColor = centerColors[room.getX() + (room.getZ() * 11)] & 0xFF;
        room.setState(getRoomState(centerColor, room.getData().type(), room));
    }

    public int getCenterColour(int arrayX, int arrayY) {
        return centerColors[arrayY * 11 + arrayX] & 0xFF;
    }
}
