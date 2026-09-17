package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.type.Vec2i;
import com.ricedotwho.rsm.utils.WorldUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

@Getter
public class Room {
    private final int roofHeight;
    private final int bottom;
    private final Vec2i position;
    @Setter
    private RoomData data;
    @Setter
    private int core = 0;
    @Setter
    private UniqueRoom uniqueRoom;
    private final ArrayList<Vec2i> unscannedDoors;
    final Vec2i pos;

    private static final int[] BEDROCKS = { 66, 67 };
    private static final int[] SOLIDS = { 68, 73 };
    private static final int[] AIRS = { 69, 70, 71, 72 };

    private static final String WITHER_SKULL_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2JjYmJmOTRkNjAzNzQzYTFlNzE0NzAyNmUxYzEyNDBiZDk4ZmU4N2NjNGVmMDRkY2FiNTFhMzFjMzA5MTRmZCJ9fX0=";
    private static final String BLOOD_SKULL_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5ZDgwYjc5NDQyY2YxYTNhZmVhYTIzN2JkNmFkYWFhY2FiMGMyODgzMGZiMzZiNTcwNGNmNGQ5ZjU5MzdjNCJ9fX0=";

    Room(int x, int z) {
        this.position = new Vec2i(x, z);
        this.roofHeight = 0;
        this.bottom = 0;
        this.pos = new Vec2i(x, z);
        unscannedDoors = new ArrayList<>();
    }

    Room(int x, int z, int dx, int dz, int roofHeight, int bottom, RoomData data) {
        this.position = new Vec2i(x, z);
        this.roofHeight = roofHeight;
        this.bottom = bottom;
        this.data = data;
        this.pos = new Vec2i(dx, dz);

        unscannedDoors = new ArrayList<>();
        unscannedDoors.add(new Vec2i(x + 16, z));
        unscannedDoors.add(new Vec2i(x, z + 16));
        unscannedDoors.add(new Vec2i(x - 16, z));
        unscannedDoors.add(new Vec2i(x, z - 16));
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
                "}";
    }

    public int getX() {
        return position.x();
    }

    public int getZ() {
        return position.y();
    }

    public void addToUnique(int x, int z, String roomName) {
        UniqueRoom unique = DungeonInfo.getUnique(roomName);

        if (unique == null) {
            DungeonInfo.addUnique(new UniqueRoom(x, z, this));
        } else {
            unique.addTile(x, z, this);
        }
    }

    public void addToUnique(int x, int z) {
        addToUnique(x, z, data.name());
    }

    public void scanForDoors() {
        if (unscannedDoors.isEmpty()) return;
        var iterator = unscannedDoors.iterator();

        while (iterator.hasNext()) {
            var door = iterator.next();

            var isDoor = isDoor(door);
            if (isDoor == null) continue;

            iterator.remove();
            if (!isDoor) continue;

            var type = classifyDoor(door);
            var rotation = getPositionDirection(door);

            uniqueRoom.addDoor(door, type, rotation);
        }
    }

    private DoorType classifyDoor(Vec2i door) {
        if (this.data.type() == RoomType.ENTRANCE) return DoorType.ENTRANCE;

        var pos = new BlockPos(door.x - 2, 70, door.y - 2);
        var skullTexture = pos.getSkullTextureID();


        if (skullTexture == null) {
            return DoorType.NORMAL;
        } else if (WITHER_SKULL_ID.equals(skullTexture)) {
            return DoorType.WITHER;
        } else if (BLOOD_SKULL_ID.equals(skullTexture)) {
            return DoorType.BLOOD;
        }

        return DoorType.NORMAL;
    }

    private @Nullable Boolean isDoor(Vec2i pos) {
        for (int y : BEDROCKS) {
            var block = WorldUtils.getBlockAt(getBlockPos(pos, y));
            if (block == null) return null;
            if (block != Blocks.BEDROCK) return false;
        }
        // chunk can no longer be null

        for (int y : SOLIDS) {
            if (WorldUtils.isBlockOrDefault(getBlockPos(pos, y), false, Blocks.AIR)) return false;
        }

        for (int y : AIRS) {
            if (!WorldUtils.isBlockOrDefault(getBlockPos(pos, y), false, Blocks.AIR, Blocks.COAL_BLOCK, Blocks.RED_TERRACOTTA, Blocks.INFESTED_CHISELED_STONE_BRICKS)) return false;
        }

        return true;
    }

    public BlockPos getBlockPos(Vec2i vec, int y) {
        return new BlockPos(vec.x, y, vec.y);
    }

    private RoomRotation getPositionDirection(Vec2i pos) {
        if (pos.x < this.position.x) {
            return RoomRotation.EAST;
        } else if (pos.x > this.position.x) {
            return RoomRotation.WEST;
        } else if (pos.y > this.position.y) {
            return RoomRotation.SOUTH;
        } else if (pos.y < this.position.y) {
            return RoomRotation.NORTH;
        } else {
            return RoomRotation.NORTH;
        }
    }
}
