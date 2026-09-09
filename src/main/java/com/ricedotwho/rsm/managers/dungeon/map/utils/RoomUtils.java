package com.ricedotwho.rsm.managers.dungeon.map.utils;

import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.managers.dungeon.map.Map;
import com.ricedotwho.rsm.managers.dungeon.map.map.Room;
import com.ricedotwho.rsm.managers.dungeon.map.map.RoomRotation;
import com.ricedotwho.rsm.managers.dungeon.map.map.RoomType;
import com.ricedotwho.rsm.managers.dungeon.map.map.UniqueRoom;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Rotation;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import static com.ricedotwho.rsm.managers.dungeon.map.map.RoomRotation.TOPLEFT;
import net.minecraft.world.phys.Vec3;

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
        return getRoofHeight(room.getX(), room.getZ(), mc.level.getChunk(new BlockPos(room.getX(), 0, room.getZ())));
    }

    public int getRoofHeight(int x, int z) {
        assert mc.level != null;
        return getRoofHeight(x, z, mc.level.getChunk(new BlockPos(x, 0, z)));
    }

    public int getRoofHeight(int x, int z, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, 67, z);
        assert mc.level != null;
        if (!mc.level.isLoaded(mutable)) return -1;
        for (int y = 160; y > 12; y--) {
            mutable.set(x, y, z);
            BlockState state = chunk.getBlockState(mutable);
            if (!state.isAir()) return state.getBlock().equals(Blocks.GOLD_BLOCK) ? y - 1 : y;
        }
//        RSM.getLogger().error("Failed to find height for x: {}, z: {}", x, z); //FUCK YOU LOGGER
        return -1;
    }

    public int getRoomBottom(Room room) {
        assert mc.level != null;
        return getRoomBottom(room.getX(), room.getZ(), mc.level.getChunk(new BlockPos(room.getX(), 0, room.getZ())));
    }

    public int getRoomBottom(int x, int z, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, 67, z);
        assert mc.level != null;
        if (!mc.level.isLoaded(mutable)) return -1;
        for (int y = 0; y < 80; y++) {
            mutable.set(x, y, z);
            BlockState state = chunk.getBlockState(mutable);
            if (!state.isAir()) return y;
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

        for (Room c : uniqueRoom.getTiles()) { // each tile in the room
            if (c.isSeparator()) continue;

            int required = switch (c.getData().shape()) {
                case S4x1 -> 4;
                case S3x1 -> 3;
                default -> 0;
            };

            //ChatUtils.chat("Room: {}, Shape {}, size: {}, required: {}", uniqueRoom.getName(), c.getData().shape(), size, required);

            if (required > 0 && uniqueRoom.realSize() != required) continue;

            for (int i = 0; i < offsets.length; i++) { // offset to get each corner of the rooms
                BlockPos nPos = new BlockPos(c.getX() + offsets[i][0], c.getRoofHeight(), c.getZ() + offsets[i][1]);

                assert mc.level != null;
                if (!mc.level.isLoaded(nPos)) {
                    uniqueRoom.setRotation(RoomRotation.UNKNOWN);
                    if (c.getData().type().equals(RoomType.ENTRANCE)) {
                        findEntranceRotation(uniqueRoom, 0);
                    }
                    return;
                }

                //if (uniqueRoom.getRotation() != null && uniqueRoom.getRotation().equals(UNKNOWN)) RSM.getLogger().info("Center x: {} z: {}. Scanning offset for room {}. Block is: {} at {}. Corner: {}", c.getX(), c.getZ(), c.getData().name(), mc.level.getBlockState(nPos).getBlock().getName().getString(), new Pos(nPos).toChatString(), isCorner(nPos));

                if (mc.level.getBlockState(nPos).getBlock().equals(Blocks.BLUE_TERRACOTTA) && isCorner(nPos)) { // rarely rooms have no gap ??
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
                    Scheduler.schedule(TickEvent.ClientStart.class, 0, () -> findEntranceRotation(uniqueRoom, tries + 1));
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
        Scheduler.schedule(TickEvent.ClientStart.class, () -> findEntranceRotation(uniqueRoom, tries + 1));
    }

    /**
     * Rotates the pos to world relative, overload for {@link #rotateReal(Vec3 pos, Room room)}
     * @param vec3 The position
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateReal(Vec3 vec3) {
        return rotateReal(vec3, Map.getCurrentRoom());
    }

    /**
     * Rotates the pos to world relative, overload for {@link #rotateReal(Vec3 pos, RoomRotation rot)}
     * @param vec3 The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateReal(Vec3 vec3, Room room) {
        return rotateReal(vec3, room.getUniqueRoom().getRotation());
    }

    /**
     * Rotates the pos to world relative
     * @param vec3 The position
     * @param rot The rotation to use
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateReal(Vec3 vec3, RoomRotation rot) {
        if (rot == TOPLEFT) return vec3;
        val x = vec3.x() - 0.5d;
        val y = vec3.y();
        val z = vec3.z() - 0.5d;
        var posX = x;
        var posZ = z;

        switch(rot) {
            // TOPLEFT already handled

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                posX = -z;
                posZ = x;
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                posX = -x;
                posZ = -z;
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                posX = z;
                posZ = -x;
                break;

            case UNKNOWN:
                break;
        }
        return new Vec3(posX + 0.5, y, posZ + 0.5);
    }

    /**
     * Rotates the pos to world relative, overload for {@link #rotateRealFixed(Vec3 pos, Room room)}
     * @param vec3 The position
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRealFixed(Vec3 vec3) {
        return rotateRealFixed(vec3, Map.getCurrentRoom());
    }

    /**
     * Rotates the pos to world relative, overload for {@link #rotateRealFixed(Vec3 pos, RoomRotation rot)}
     * @param vec3 The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRealFixed(Vec3 vec3, Room room) {
        return rotateRealFixed(vec3, room.getUniqueRoom().getRotation());
    }

    /**
     * Rotates the pos to world relative, overload for {@link #rotateRealFixed(BlockPos pos, RoomRotation rot)}
     * @param pos The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position
     */
    public BlockPos rotateRealFixed(BlockPos pos, Room room) {
        return rotateRealFixed(pos, room.getUniqueRoom().getRotation());
    }

    public Vec3 rotateRealFixed(Vec3 vec3, RoomRotation rot) {
        if (rot == TOPLEFT) return vec3;
        double x = vec3.x();
        double y = vec3.y();
        double z = vec3.z();
        var posX = x;
        var posZ = z;
        switch(rot) {
            // TOPLEFT already handled

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                posX = -z;
                posZ = x;
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                posX = -x;
                posZ = -z;
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                posX = z;
                posZ = -x;
                break;

            case UNKNOWN:
                break;
        }
        return new Vec3(posX, y, posZ);
    }

    public BlockPos rotateRealFixed(BlockPos pos, RoomRotation rot) {
        if (rot == TOPLEFT) return pos;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos newPos = pos;
        switch(rot) {
            // TOPLEFT already handled

            case TOPRIGHT: // Rotate 90°
                // x,z = -z,x
                newPos = new BlockPos(-z, y, x);
                break;

            case BOTRIGHT: // Rotate 180°
                // x,z = -x,-z
                newPos = new BlockPos(-x, y, -z);
                break;

            case BOTLEFT: // Rotate 270°
                // x,z = z,-x
                newPos = new BlockPos(z, y, -x);
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
     * Rotates the pos to room relative, overload for {@link #rotateRelative(Vec3 pos, Room room)}
     * @param vec3 The position
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRelative(Vec3 vec3) {
        return rotateRelative(vec3, Map.getCurrentRoom());
    }

    /**
     * Rotates the pos to room relative, overload for {@link #rotateRelative(Vec3 pos, RoomRotation rot)}
     * @param vec3 The position
     * @param room The room to use for the rotation
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRelative(Vec3 vec3, Room room) {
        return rotateRelative(vec3, room.getUniqueRoom().getRotation());
    }

    /**
     * Rotates the pos to room relative
     * @param vec3 The position
     * @param rot The rotation to use
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRelative(Vec3 vec3, RoomRotation rot) {
        if (rot == TOPLEFT) return vec3;
        final double x = vec3.x() - 0.5d;
        final double y = vec3.y();
        final double z = vec3.z() - 0.5d;
        var posX = x;
        var posZ = z;

        // We are undoing rotations, so all this needs to be ANTI-clockwise (or just -)
        switch(rot) {
            // TOPLEFT is already handled

            case TOPRIGHT: // Rotate -90°
                // x,z = z,-x
                posX = z;
                posZ = -x;
                break;

            case BOTRIGHT: // Rotate -180°
                // x,z = -x,-z
                posX = -x;
                posZ = -x;
                break;

            case BOTLEFT: // Rotate -270°
                // x,z = -z,x
                posX = -z;
                posZ = x;
                break;

            case UNKNOWN:
                return null;
        }
        return new Vec3(posX + 0.5, y, posZ + 0.5);
    }

    /**
     * Rotates the pos to room relative, overload for {@link #rotateRelativeFixed(Vec3 pos, Room room)}
     * @param vec3 The position
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRelativeFixed(Vec3 vec3) {
        return rotateRelativeFixed(vec3, Map.getCurrentRoom());
    }

    /**
     * Rotates the pos to room relative, overload for {@link #rotateRelativeFixed(Vec3 pos, RoomRotation rot)}
     * @param vec3 The position
     * @param room The room to use for the rotation
     * @return {@link Vec3} the rotated position
     */
    public Vec3 rotateRelativeFixed(Vec3 vec3, Room room) {
        return rotateRelativeFixed(vec3, room.getUniqueRoom().getRotation());
    }

    public Vec3 rotateRelativeFixed(Vec3 vec3, RoomRotation rot) {
        if (rot == TOPLEFT) return vec3;
        double x = vec3.x();
        double y = vec3.y();
        double z = vec3.z();
        var posX = x;
        var posZ = z;

        // We are undoing rotations, so all this needs to be ANTI-clockwise (or just -)
        switch(rot) {
            // TOPLEFT is already handled

            case TOPRIGHT: // Rotate -90°
                // x,z = z,-x
                posX = z;
                posZ = -x;
                break;

            case BOTRIGHT: // Rotate -180°
                // x,z = -x,-z
                posX = -x;
                posZ = -z;
                break;

            case BOTLEFT: // Rotate -270°
                // x,z = -z,x
                posX = -z;
                posZ = x;
                break;

            case UNKNOWN:
                return null;
        }
        return new Vec3(posX, y, posZ);
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
     * Get the room relative position, overload for {@link #getRelativePosition(Vec3 pos, Room room)}
     * @param blockPos The block position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public Vec3 getRelativePosition(BlockPos blockPos, Room room) {
        if(blockPos == null) return null;
        return getRelativePosition(new Vec3(blockPos), room);
    }

    /**
     * Get the room relative position
     * @param vec3 The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public Vec3 getRelativePosition(Vec3 vec3, Room room) {
        if (vec3 == null) return null;
        if (room == null) return vec3;
        return rotateRelative(new Vec3(vec3.x() - room.getX(), vec3.y(), vec3.z() - room.getZ()), room);
    }

    /**
     * Get the room relative position, overload for {@link #getRelativePositionFixed(Vec3 pos, Room room)}
     * @param blockPos The block position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public Vec3 getRelativePositionFixed(BlockPos blockPos, Room room) {
        if(blockPos == null) return null;
        return getRelativePositionFixed(new Vec3(blockPos), room);
    }

    /**
     * Get the room relative position
     * @param vec3 The position
     * @param room The room to use the rotation of,
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public Vec3 getRelativePositionFixed(Vec3 vec3, Room room) {
        if (vec3 == null) return null;
        if (room == null) return vec3;
        return rotateRelativeFixed(new Vec3(vec3.x() - room.getX(), vec3.y(), vec3.z() - room.getZ()), room);
    }

    /**
     * Get the real position, overload for {@link #getRealPosition(Vec3 fpos, Room room)}
     * @param fpos The block position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public BlockPos getRealPosition(BlockPos fpos, Room room) {
        Vec3 vec3 = getRealPosition(new Vec3(fpos.getX() + 0.5, fpos.getY(), fpos.getZ() + 0.5), room);
        return new BlockPos((int) Math.floor(vec3.x), (int) Math.floor(vec3.y), (int) Math.floor(vec3.z));
    }

    /**
     * Get the real position
     * @param fpos The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public Vec3 getRealPosition(Vec3 fpos, Room room) {
        if (fpos == null) return null;
        Vec3 gpos = rotateReal(fpos, room);
        return new Vec3(gpos.x() + room.getX(), gpos.y(), gpos.z() + room.getZ());
    }

    /**
     * Get the real position
     * @param fpos The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public Vec3 getRealPositionFixed(Vec3 fpos, Room room) {
        if (fpos == null) return null;
        Vec3 gpos = rotateRealFixed(fpos, room);
        return new Vec3(gpos.x() + room.getX(), gpos.y(), gpos.z() + room.getZ());
    }

    /**
     * Get the real position
     * @param fpos The position
     * @param room The room to use the rotation of
     * @return {@link Vec3} the rotated position, or null if the room or pos is null
     */
    public BlockPos getRealPositionFixed(BlockPos fpos, Room room) {
        if (fpos == null) return null;
        BlockPos gpos = rotateRealFixed(fpos, room);
        return new BlockPos(gpos.getX() + room.getX(), gpos.getY(), gpos.getZ() + room.getZ());
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
     * Get the relative yaw, overload for {@link #getRealYaw(Rotation rotation, RoomRotation roomRotation)}
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

    public float getRelativeYaw(float yaw, RoomRotation roomRotation) {
        return switch (roomRotation) {
            case TOPRIGHT -> // Rotate -90°
                    RotationUtils.wrapAngleTo180(yaw - 90);
            case BOTRIGHT -> // Rotate -180°
                    RotationUtils.wrapAngleTo180(yaw - 180);
            case BOTLEFT -> // Rotate -270°
                    RotationUtils.wrapAngleTo180(yaw - 270);
            default -> yaw;
        };
    }

    public float getRealYaw(float yaw, RoomRotation roomRotation) {
        return switch (roomRotation) {
            case TOPRIGHT -> // Rotate 90°
                    RotationUtils.wrapAngleTo180(yaw + 90);
            case BOTRIGHT -> // Rotate 180°
                    RotationUtils.wrapAngleTo180(yaw + 180);
            case BOTLEFT -> // Rotate 270°
                    RotationUtils.wrapAngleTo180(yaw + 270);
            default -> yaw;
        };
    }

    public Direction getRelativeDirection(Direction direction, RoomRotation roomRotation) {
        if (direction == Direction.DOWN || direction == Direction.UP) return direction;

        return switch (roomRotation) {
            case TOPRIGHT ->
                    direction.getClockWise();
            case BOTRIGHT ->
                    direction.getOpposite();
            case BOTLEFT ->
                    direction.getCounterClockWise();
            default -> direction;
        };
    }

    public Direction getRealDirection(Direction direction, RoomRotation roomRotation) {
        if (direction == Direction.DOWN || direction == Direction.UP) return direction;

        return switch (roomRotation) {
            case TOPRIGHT ->
                    direction.getCounterClockWise();
            case BOTRIGHT ->
                    direction.getOpposite();
            case BOTLEFT ->
                    direction.getClockWise();
            default -> direction;
        };
    }
}
