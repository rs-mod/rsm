package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.map.*;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class DungeonScanner implements Accessor {
    public final int roomSize = 32;
    public final int startX = -185;
    public final int startZ = -185;
    private long lastScanTime = 0;
    private boolean isScanning = false;
    public boolean hasScanned = false;

    public static final List<Pair<Integer, Integer>> OFFSETS = Arrays.asList(
            new Pair<>(0, 2),
            new Pair<>(0, -2),
            new Pair<>(-2, 0),
            new Pair<>(2, 0)
    );

    public void reset() {
        hasScanned = false;
        isScanning = false;
    }

    public boolean shouldScan() {
        return !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && Location.getFloor() != Floor.None;
    }

    public void scan() {
        isScanning = true;
        boolean allChunksLoaded = true;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        assert mc.level != null;

        // 11x11 grid
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                int xPos = startX + x * (roomSize >> 1);
                int zPos = startZ + z * (roomSize >> 1);
                mutable.set(xPos, 67, zPos);

                if (!mc.level.isLoaded(mutable)) {
                    allChunksLoaded = false;
                    continue;
                }

                // this room has already been added in a previous scan.
                if (DungeonInfo.getDungeonList()[x + z * 11] instanceof Room room) {
                    if (!room.isSeparator() && !room.getData().name().equals("Unknown")) continue;
                }

                Tile result = scanRoom(xPos, zPos, z, x);
                if (result != null) {
                    DungeonInfo.getDungeonList()[z * 11 + x] = result;
                }
            }
        }

        if (allChunksLoaded && DungeonInfo.getUniqueRooms().stream().noneMatch(r  -> r.getRotation().equals(RoomRotation.UNKNOWN))) {
            DungeonInfo.setRoomCount(((int) Arrays.stream(DungeonInfo.getDungeonList()).filter(tile -> tile instanceof Room && !((Room) tile).isSeparator()).count()));;
            hasScanned = true;
            new DungeonEvent.ScanComplete().post();
        }

        lastScanTime = System.currentTimeMillis();
        isScanning = false;
    }

    private Tile scanRoom(int x, int z, int row, int column) {
        assert mc.level != null;
        int height = RoomUtils.getRoofHeight(x, z);
        if (height == 0) {
            RSM.getLogger().warn("Height 0 at x: {} z: {}", x, z);
            return null;
        }

        boolean rowEven = (row & 1) == 0;
        boolean columnEven = (column & 1) == 0;

        if (rowEven && columnEven) {
            ChunkAccess chunk = mc.level.getChunk(new BlockPos(x, 0, z));
            int roomHeight = RoomUtils.getRoofHeight(x, z, chunk);
            int bottom = RoomUtils.getRoomBottom(x, z, chunk);

            int roomCore = ScanUtils.getCore(x, z, roomHeight, chunk);
            RoomData roomData = ScanUtils.getRoomData(roomCore);
            if (roomData == null) {
                //RSM.getLogger().warn("RoomData is null for {} at x: {}, z: {}", roomCore, x, z);
                return null;
            }
            Room room = new Room(x, z, roomHeight, bottom, roomData);
            room.setCore(roomCore);
            room.addToUnique(row, column);
            if (DungeonInfo.getRoomList().add(room)) {
                new DungeonEvent.RoomLoad(room).post();
            }
            return room;
        } else if (!rowEven && !columnEven) {
            Tile tile = DungeonInfo.getDungeonList()[column - 1 + (row - 1) * 11];
            if (tile instanceof Room) {
                Room room = new Room(x, z, ((Room) tile).getData());
                room.setSeparator(true);
                room.addToUnique(row, column);
                DungeonInfo.getRoomList().add(room);
                return room;
            }
            return null;
        } else if (height == 74 || height == 82 || height == 73) { // 73?
            DoorType doorType;
            Block block = mc.level.getBlockState(new BlockPos(x, 69, z)).getBlock();
            if (block.equals(Blocks.COAL_BLOCK)) {
                DungeonInfo.setWitherDoors(DungeonInfo.getWitherDoors() + 1);
                doorType = DoorType.WITHER;
            } else if (block.equals(Blocks.INFESTED_CHISELED_STONE_BRICKS)) {
                doorType = DoorType.ENTRANCE;
            } else if (block.equals(Blocks.RED_TERRACOTTA)) {
                doorType = DoorType.BLOOD;
            } else {
                doorType = DoorType.NORMAL;
            }
            Door door = new Door(x, z, doorType);

            // adding the door to uniques
            for (Pair<Integer, Integer> pair : OFFSETS) {
                int rx = x + pair.getFirst();
                int rz = z + pair.getSecond();
                Room room = ScanUtils.getRoomFromPos(rx, rz);
                if (room == null || room.getUniqueRoom() == null) continue;
                room.getUniqueRoom().addDoor(door);
            }

            DungeonInfo.getDoorList().add(door);
            return door;
        } else {
            int index = rowEven ? row * 11 + column - 1 : (row - 1) * 11 + column;
            Tile tile = DungeonInfo.getDungeonList()[index];
            if (tile instanceof Room room) {
                if (room.getData().type() == RoomType.ENTRANCE && (height == 99 || height == 98)) {
//                    Door door = new Door(x, z, DoorType.ENTRANCE);
//                    DungeonInfo.getDoorList().add(door);
                    return new Door(x, z, DoorType.ENTRANCE);
                } else {
                    Room separator = new Room(x, z, room.getData());
                    separator.setSeparator(true);
                    return separator;
                }
            }
            return null;
        }
    }
}
