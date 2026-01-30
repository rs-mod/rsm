package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.component.impl.map.map.*;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.mixins.accessor.AccessorLevelChunk;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Arrays;

public class DungeonScanner implements Accessor {
    // The size of each dungeon room in blocks.
    public static final int roomSize = 32;

    // The starting coordinates to start scanning (the north-west corner).
    public static final int startX = -185;
    public static final int startZ = -185;

    private static long lastScanTime = 0;
    private static boolean isScanning = false;
    public static boolean hasScanned = false;

    public static boolean shouldScan() {
        return !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && Loc.floor != Floor.None;
    }

    public static void scan() {
        isScanning = true;
        boolean allChunksLoaded = true;

        // 11x11 grid
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                int xPos = startX + x * (roomSize >> 1);
                int zPos = startZ + z * (roomSize >> 1);

                if (!((AccessorLevelChunk) mc.level.getChunk(xPos >> 4, zPos >> 4)).isLoaded()) {
                    // unloaded
                    allChunksLoaded = false;
                    continue;
                }

                // this room has already been added in a previous scan.
                if (DungeonInfo.dungeonList[x + z * 11] instanceof Room) {
                    Room room = (Room) DungeonInfo.dungeonList[x + z * 11];
                    if (!room.isSeparator() && !room.getData().getName().equals("Unknown")) continue;
                }

                Tile result = scanRoom(xPos, zPos, z, x);
                if (result != null) {
                    DungeonInfo.dungeonList[z * 11 + x] = result;
                }
            }
        }

        if (allChunksLoaded) {
            DungeonInfo.roomCount = ((int) Arrays.stream(DungeonInfo.dungeonList).filter(tile -> tile instanceof Room && !((Room) tile).isSeparator()).count());
            hasScanned = true;
        }

        lastScanTime = System.currentTimeMillis();
        isScanning = false;
    }

    private static Tile scanRoom(int x, int z, int row, int column) {
        int height = mc.level.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if (height == 0) return null;

        boolean rowEven = (row & 1) == 0;
        boolean columnEven = (column & 1) == 0;

        if (rowEven && columnEven) {
            int roomCore = ScanUtils.getCore(x, z);
            RoomData roomData = ScanUtils.getRoomData(roomCore);
            if (roomData == null) return null;
            Room room = new Room(x, z, roomData);
            room.setCore(roomCore);
            room.addToUnique(row, column);
            DungeonInfo.roomList.add(room);

            return room;
        } else if (!rowEven && !columnEven) {
            Tile tile = DungeonInfo.dungeonList[column - 1 + (row - 1) * 11];
            if (tile instanceof Room) {
                Room room = new Room(x, z, ((Room) tile).getData());
                room.setSeparator(true);
                room.addToUnique(row, column);
                DungeonInfo.roomList.add(room);
                return room;
            }
            return null;
        } else if (height == 74 || height == 82) {
            DoorType doorType;
            // Switch wont work here idk why
            Block block = mc.level.getBlockState(new BlockPos(x, 69, z)).getBlock();
            if (block.equals(Blocks.COAL_BLOCK)) {
                DungeonInfo.witherDoors++;
                doorType = DoorType.WITHER;
            } else if (block.equals(Blocks.INFESTED_CHISELED_STONE_BRICKS)) {
                doorType = DoorType.ENTRANCE;
            } else if (block.equals(Blocks.RED_TERRACOTTA)) {
                doorType = DoorType.BLOOD;
            } else {
                doorType = DoorType.NORMAL;
            }
            return new Door(x, z, doorType);
        } else {
            int index = rowEven ? row * 11 + column - 1 : (row - 1) * 11 + column;
            Tile tile = DungeonInfo.dungeonList[index];
            if (tile instanceof Room room) {
                if (room.getData().getType() == RoomType.ENTRANCE) {
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
