package com.ricedotwho.rsm.component.impl.map.utils;

import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomRotation;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import static com.ricedotwho.rsm.component.impl.map.map.RoomRotation.TOPLEFT;

@UtilityClass
public class RoomUtils implements Accessor {
    private final int[][] offsets = {{-15, -15}, {15, -15}, {15, 15}, {-15, 15}};
    private final int[][] cornerOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    // what the freak yo
    private final int[][] entranceOffsets = {
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

    private RoomRotation getRotationByNumber(int rot) {
        return switch (rot) {
            case 0 -> TOPLEFT;
            case 1 -> RoomRotation.TOPRIGHT;
            case 2 -> RoomRotation.BOTRIGHT;
            case 3 -> RoomRotation.BOTLEFT;
            default -> RoomRotation.UNKNOWN;
        };
    }

    public int getRoofHeight(Room room) {
        assert mc.level != null;
        return getRoofHeight(room.getX(), room.getZ(), mc.level.getChunk(room.getX(), room.getZ()));
    }

    public int getRoofHeight(int x, int z, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = 160; y > 12; y--) {
            mutable.set(x, y, z);
            BlockState state = chunk.getBlockState(mutable);
            if (!state.isAir()) return state.getBlock().equals(Blocks.GOLD_BLOCK) ? y - 1 : y;
        }
        return -1;
    }

    public void findMainAndRotation(UniqueRoom uniqueRoom){
        if (uniqueRoom.getTiles().isEmpty()) return;

        Room room = uniqueRoom.getTiles().getFirst();

        if (room.getData().type().equals(RoomType.FAIRY)) {
            uniqueRoom.setRotation(TOPLEFT);
            uniqueRoom.setMainRoom(room);
            return;
        }

        if (room.getData().type().equals(RoomType.ENTRANCE) && !room.getData().name().equals("Entrance 2")) { // entrance 1 and 3 have weird sizes
            findEntranceRotation(uniqueRoom, 0);
            return;
        }


        for(Room c : uniqueRoom.getTiles()) { // each tile in the room
            for (int i = 0; i < offsets.length; i++) { // the offsets are just the coords to get to the corners of the room lol
                BlockPos nPos = new BlockPos(c.getX() + offsets[i][0], c.getRoofHeight(), c.getZ() + offsets[i][1]);

                assert mc.level != null;
                if (!mc.level.isLoaded(nPos)) {
                    uniqueRoom.setRotation(RoomRotation.UNKNOWN);
                    if (c.getData().type().equals(RoomType.ENTRANCE)) {
                        findEntranceRotation(uniqueRoom, 0);
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
        if (uniqueRoom.getTiles().getFirst().getData().type().equals(RoomType.ENTRANCE)) {
            findEntranceRotation(uniqueRoom, 0);
        }
    }

    private void findEntranceRotation(UniqueRoom uniqueRoom, int tries) {
        if (tries > 20) return;
        for(Room c : uniqueRoom.getTiles()) {
            for (int i = 0; i < entranceOffsets.length; i++) {
                BlockPos nPos = new BlockPos(c.getX() + entranceOffsets[i][0], c.getRoofHeight(), c.getZ() + entranceOffsets[i][1]);

                assert mc.level != null;
                if(!mc.level.isLoaded(nPos)) {
                    uniqueRoom.setRotation(RoomRotation.UNKNOWN);
                    TaskComponent.onTick(0, () -> findEntranceRotation(uniqueRoom, tries + 1));
                    return;
                }

                Room atPos = ScanUtils.getRoomFromPos(nPos.getX(), nPos.getZ());
                if (atPos != null && atPos.getData().type() != RoomType.ENTRANCE) {
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

    /**
     * Rotates the pos to world relative, overload for {@link #rotateReal(Pos pos, Room room)}
     * @param pos The position
     * @return {@link Pos} the rotated position
     */
    public Pos rotateReal(Pos pos) {
        return rotateReal(pos, Map.getCurrentRoom());
    }

    /**
     * Rotates the pos to world relative, overload for {@link #rotateReal(Pos pos, RoomRotation rot)}
     * @param pos The position
     * @param room The room to use the rotation of
     * @return {@link Pos} the rotated position
     */
    public Pos rotateReal(Pos pos, Room room) {
        return rotateReal(pos, room.getUniqueRoom().getRotation());
    }

    /**
     * Rotates the pos to world relative
     * @param pos The position
     * @param rot The rotation to use
     * @return {@link Pos} the rotated position
     */
    public Pos rotateReal(Pos pos, RoomRotation rot) {
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();
        Pos newPos = pos.copy();
        switch(rot) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                newPos.set(-z, y, x);
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                newPos.set(-x, y, -z);
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                newPos.set(z, y, -x);
                break;

            case UNKNOWN:
                break;
        }
        return newPos;
    }

    public BlockPos rotateReal(BlockPos.MutableBlockPos pos, Room room) {
        RoomRotation rot = room.getUniqueRoom().getRotation();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch(rot) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                pos.set(-z, y, x);
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                pos.set(-x, y, -z);
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                pos.set(z, y, -x);
                break;

            case UNKNOWN:
                break;
        }
        return pos;
    }

    /**
     * Rotates the pos to room relative, overload for {@link #rotateRelative(Pos pos, Room room)}
     * @param pos The position
     * @return {@link Pos} the rotated position
     */
    public Pos rotateRelative(Pos pos) {
        return rotateRelative(pos, Map.getCurrentRoom());
    }

    /**
     * Rotates the pos to room relative, overload for {@link #rotateRelative(Pos pos, RoomRotation rot)}
     * @param pos The position
     * @param room The room to use for the rotation
     * @return {@link Pos} the rotated position
     */
    public Pos rotateRelative(Pos pos, Room room) {
        return rotateRelative(pos, room.getUniqueRoom().getRotation());
    }

    /**
     * Rotates the pos to room relative
     * @param pos The position
     * @param rot The rotation to use
     * @return {@link Pos} the rotated position
     */
    public Pos rotateRelative(Pos pos, RoomRotation rot) {
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

    /// tbh I forgot why this is important
    private boolean isCorner(BlockPos pos) {
        int counter = 0;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        assert mc.level != null;
        for (int[] cornerOffset : cornerOffsets) {
            if (mc.level.getBlockState(new BlockPos(x + cornerOffset[0], y, z + cornerOffset[1])).getBlock() != Blocks.AIR) counter++;
        }
        return counter <= 2;
    }

    /**
     * Get the room relative position, overload for {@link #getRelativePosition(Pos pos, Room room)}
     * @param blockPos The block position
     * @param room The room to use the rotation of
     * @return {@link Pos} the rotated position, or null if the room or pos is null
     */
    public Pos getRelativePosition(BlockPos blockPos, Room room) {
        if(blockPos == null) return null;
        return getRelativePosition(new Pos(blockPos), room);
    }

    /**
     * Get the room relative position
     * @param pos The position
     * @param room The room to use the rotation of
     * @return {@link Pos} the rotated position, or null if the room or pos is null
     */
    public Pos getRelativePosition(Pos pos, Room room) {
        if (pos == null) return null;
        if (room == null) return pos;
        return rotateRelative(new Pos(pos.x() - room.getX(), pos.y(), pos.z() - room.getZ()), room);
    }

    /**
     * Get the room relative position, overload for {@link #getRealPosition(Pos fpos, Room room)}
     * @param fpos The block position
     * @param room The room to use the rotation of
     * @return {@link Pos} the rotated position, or null if the room or pos is null
     */
    public Pos getRealPosition(BlockPos fpos, Room room) {
        return getRealPosition(new Pos(fpos), room);
    }

    /**
     * Get the real position
     * @param fpos The position
     * @param room The room to use the rotation of
     * @return {@link Pos} the rotated position, or null if the room or pos is null
     */
    public Pos getRealPosition(Pos fpos, Room room) {
        if (fpos == null) return null;
        Pos gpos = rotateReal(fpos, room);
        return new Pos(gpos.x() + room.getX(), gpos.y(), gpos.z() + room.getZ());
    }


    /**
     * Get the real yaw, overload for {@link #getRealYaw(Rotation rotation, RoomRotation roomRotation)}
     * @param rotation The rotation
     * @return {@link Rotation} the rotated rotation
     */
    public Rotation getRealYaw(Rotation rotation) {
        if(Map.getCurrentRoom() == null) return rotation;
        return getRealYaw(rotation, Map.getCurrentRoom().getUniqueRoom().getRotation());
    }

    /**
     * Get the real yaw
     * @param rotation The rotation
     * @param roomRotation The room rotation
     * @return {@link Rotation} the rotated rotation
     */
    public Rotation getRealYaw(Rotation rotation, RoomRotation roomRotation) {
        Rotation rot = new Rotation(rotation.getPitch(), rotation.getYaw());
        switch (roomRotation) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate 90°
                rot.setYaw(RotationUtils.wrapAngleTo180(rotation.getYaw() + 90));
                break;

            case BOTRIGHT: // Rotate 180°
                rot.setYaw(RotationUtils.wrapAngleTo180(rotation.getYaw() + 180));
                break;

            case BOTLEFT: // Rotate 270°
                rot.setYaw(RotationUtils.wrapAngleTo180(rotation.getYaw() + 270));
                break;
        }
        return rot;
    }

    /**
     * Get the real yaw, overload for {@link #getRealYaw(Rotation rotation, RoomRotation roomRotation)}
     * @param rotation The rotation
     * @return {@link Rotation} the rotated rotation
     */
    public Rotation getRelativeYaw(Rotation rotation) {
        if(Map.getCurrentRoom() == null) return rotation;
        return getRelativeYaw(rotation, Map.getCurrentRoom().getUniqueRoom().getRotation());
    }

    /**
     * Get the relative yaw
     * @param rotation The rotation
     * @param roomRotation The room rotation
     * @return {@link Rotation} the rotated rotation
     */
    public Rotation getRelativeYaw(Rotation rotation, RoomRotation roomRotation) {
        Rotation rot = new Rotation(rotation.getPitch(), rotation.getYaw());
        switch (roomRotation) {
            case TOPLEFT: // Nothing
                break;

            case TOPRIGHT: // Rotate -90°
                rot.setYaw(RotationUtils.wrapAngleTo180(rotation.getYaw() - 90));
                break;

            case BOTRIGHT: // Rotate -180°
                rot.setYaw(RotationUtils.wrapAngleTo180(rotation.getYaw() - 180));
                break;

            case BOTLEFT: // Rotate -270°
                rot.setYaw(RotationUtils.wrapAngleTo180(rotation.getYaw() - 270));
                break;
        }
        return rot;
    }
}
