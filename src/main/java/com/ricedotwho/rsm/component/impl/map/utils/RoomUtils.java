package com.ricedotwho.rsm.component.impl.map.utils;

import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomRotation;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RoomUtils implements Accessor {
    public static Room playerCurrentRoom = null;
    public static Room currentMainRoom = null;
    public static void reset() {
        playerCurrentRoom = null;
    }
    private static final int[][] offsets = {{-15, -15}, {15, -15}, {15, 15}, {-15, 15}};
    private static final int[][] entranceOffsets = {
            {-15, -15},
            {15, -15},
            {15, 15},
            {-15, 15},

            {-16, -15},
            {16, -15},
            {16, 15},
            {-16, 15},

            {-15, -16},
            {15, -16},
            {15, 16},
            {-15, 16},

            {-27, -15},
            {27, -15},
            {27, 15},
            {-27, 15},

            {-15, -27},
            {15, -27},
            {15, 27},
            {-15, 27}
    };
    private static final int[][] cornerOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public static void getCurrentRoom() {
        if (mc.player == null) return;
        playerCurrentRoom = ScanUtils.getRoomFromPos((int) mc.player.position().x(), (int) mc.player.position().z());
        currentMainRoom = getMainRoom(playerCurrentRoom);
    }

    private static RoomRotation getRotationByNumber(int rot) {
        switch(rot) {
            case 0:
                return RoomRotation.TOPLEFT;
            case 1:
                return RoomRotation.TOPRIGHT;
            case 2:
                return RoomRotation.BOTRIGHT;
            case 3:
                return RoomRotation.BOTLEFT;
            default:
                return RoomRotation.UNKNOWN;
        }
    }

    public static int getRoofHeight(Room room) {
        int x = room.getX();
        int z = room.getZ();

        for (int y = 180; y > 80; y--) {
            // Check for a block
            Block block = mc.level.getBlockState(new BlockPos(x, y, z)).getBlock();
            if (Utils.equalsOneOf(block, Blocks.GOLD_BLOCK, Blocks.AIR)) continue;
            return y;
        }
        return 0;
    }

    public static Room getMainRoom(Room room){
        if (room == null || room.getUniqueRoom() == null) return null;
        return room.getUniqueRoom().getMainRoom();
    }

    public static void findMainAndRotation(UniqueRoom uniqueRoom) {
        findMainAndRotation(uniqueRoom, 0);
    }

    public static void findMainAndRotation(UniqueRoom uniqueRoom, int tries){
        if (uniqueRoom.getTiles().isEmpty() || tries > 20) return;

        Room room = uniqueRoom.getTiles().get(0);

        if (room.getData().getType().equals(RoomType.FAIRY)) {
            uniqueRoom.setRotation(RoomRotation.TOPLEFT);
            uniqueRoom.setMainRoom(room);
            return;
        }

        if (room.getData().getType().equals(RoomType.ENTRANCE) && !room.getData().getName().equals("Entrance 2")) { // entrance 1 and 3 have weird sizes
            findEntranceRotation(uniqueRoom, 0);
            return;
        }


        for(Room c : uniqueRoom.getTiles()) { // each tile in the room
            for (int i = 0; i < offsets.length; i++) { // the offsets are just the coords to get to the corners of the room lol
                BlockPos nPos = new BlockPos(c.getX() + offsets[i][0], c.getRoofHeight(), c.getZ() + offsets[i][1]);

                if(!mc.level.isLoaded(nPos)) {
                    uniqueRoom.setRotation(RoomRotation.UNKNOWN);
                    if (c.getData().getType().equals(RoomType.ENTRANCE)) {
                        findEntranceRotation(uniqueRoom, tries + 1);
                    }
                    return;
                }

                if (mc.level.getBlockState(nPos).getBlock().equals(Blocks.BLUE_TERRACOTTA) && isCorner(nPos)) { // i forgot what colour 11 is
                    RoomRotation rot = getRotationByNumber(i);
                    uniqueRoom.setRotation(rot);
                    uniqueRoom.setMainRoom(c);
                    return;
                }
            }
        }
        uniqueRoom.setRotation(RoomRotation.UNKNOWN);
        if (uniqueRoom.getTiles().getFirst().getData().getType().equals(RoomType.ENTRANCE)) {
            findEntranceRotation(uniqueRoom, tries + 1);
        }
    }

    private static void findEntranceRotation(UniqueRoom uniqueRoom, int tries) {
        if (tries > 20) return;
        for(Room c : uniqueRoom.getTiles()) {
            for (int i = 0; i < entranceOffsets.length; i++) {
                BlockPos nPos = new BlockPos(c.getX() + entranceOffsets[i][0], c.getRoofHeight(), c.getZ() + entranceOffsets[i][1]);

                if(!mc.level.isLoaded(nPos)) {
                    uniqueRoom.setRotation(RoomRotation.UNKNOWN);
                    TaskComponent.onTick(0, () -> findEntranceRotation(uniqueRoom, tries + 1));
                    return;
                }

                Room atPos = ScanUtils.getRoomFromPos(nPos.getX(), nPos.getZ());
                if (atPos != null && atPos.getData().getType() != RoomType.ENTRANCE) {
                    continue;
                }

                if (mc.level.getBlockState(nPos).getBlock().equals(Blocks.BLUE_TERRACOTTA)) {
                    RoomRotation rot = getRotationByNumber(i);
                    uniqueRoom.setRotation(rot);
                    uniqueRoom.setMainRoom(c);
                    return;
                }
            }
        }
        uniqueRoom.setRotation(RoomRotation.UNKNOWN);
        TaskComponent.onTick(0, () -> findEntranceRotation(uniqueRoom, tries + 1));
    }

    public static Pos getRoomCoords(Pos pos) {
        return getRoomCoords(pos, playerCurrentRoom);
    }

    public static Pos getRoomCoords(Pos pos, Room room) {
        return getRoomCoords(pos, room.getUniqueRoom().getRotation());
    }

    public static Pos getRoomCoords(Pos pos, RoomRotation rot) {
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();
        Pos newPos = pos;
        switch(rot) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                newPos = new Pos(-z,y,x);
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                newPos = new Pos(-x,y,-z);
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                newPos = new Pos(z,y,-x);
                break;

            case UNKNOWN:
                //do something here maybe, error handling? this shouldn't happen
                break;
        }
        return newPos;
    }

    public static BlockPos getRoomCoords(BlockPos pos, Room room) {
        RoomRotation rot = room.getUniqueRoom().getRotation();;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos newPos = pos;
        switch(rot) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                newPos = new BlockPos(-z,y,x);
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                newPos = new BlockPos(-x,y,-z);
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                newPos = new BlockPos(z,y,-x);
                break;

            case UNKNOWN:
                //do something here maybe, error handling? this shouldn't happen
                break;
        }
        return newPos;
    }

    public static Pos getRelativeCoords(Pos pos) {
        return getRelativeCoords(pos, RoomUtils.playerCurrentRoom);
    }

    public static Pos getRelativeCoords(Pos pos, Room room) {
        return getRelativeCoords(pos, room.getUniqueRoom().getRotation());
    }

    public static Pos getRelativeCoords(Pos pos, RoomRotation rot) {
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();
        Pos newPos = pos;

        // We are undoing rotations, so all this needs to be ANTI-clockwise (or just -)
        switch(rot) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate -90°
                // x,z = z,-x
                newPos = new Pos(z,y,-x);
                break;

            case BOTRIGHT: // Rotate -180°
                // x,z = -x,-z
                newPos = new Pos(-x,y,-z);
                break;

            case BOTLEFT: // Rotate -270°
                // x,z = -z,x
                newPos = new Pos(-z,y,x);
                break;

            case UNKNOWN:
                return null;
        }
        return newPos;
    }
    public static BlockPos getRelativeCoords(BlockPos pos, Room room) {
        Pos rpos = getRelativeCoords(new Pos(pos), room);
        if(rpos == null) return null;
        return rpos.asBlockPos();
    }

    /**
     * checks if a block is a corner or not (NOT A CHUNK!!)
     */
    public static boolean isCorner(BlockPos pos) {
        int counter = 0;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int[] cornerOffset : cornerOffsets) {
            if (mc.level.getBlockState(new BlockPos(x + cornerOffset[0], y, z + cornerOffset[1])).getBlock() != Blocks.AIR) counter++;
        }
        return counter <= 2;
    }

    public static Pos relativePosition(BlockPos blockPos, Room room) {
        if(blockPos == null) return null;
        return relativePosition(new Pos(blockPos), room);
    }

    public static Pos relativePosition(Pos pos, Room room) {
        if(pos == null) return null;
        if (room == null) return pos;
        return RoomUtils.getRelativeCoords(new Pos(pos.x() - room.getX(), pos.y(), pos.z() - room.getZ()), room);
    }

    public static Pos realPosition(BlockPos fpos, Room room) {
        return realPosition(new Pos(fpos), room);
    }

    public static Pos realPosition(Pos fpos, Room room) {
        Pos gpos = RoomUtils.getRoomCoords(fpos, room);
        return new Pos(gpos.x() + room.getX(), gpos.y(), gpos.z() + room.getZ());
    }
}
