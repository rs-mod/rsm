package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.type.DataStore;
import com.ricedotwho.rsm.type.Rotation;
import com.ricedotwho.rsm.type.Vec2i;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.CollectionUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import com.ricedotwho.rsm.utils.WorldUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;

public class UniqueRoom {
    @Getter
    private final String name;
    @Getter
    private Room mainRoom;
    @Getter
    private final List<Room> tiles = new ArrayList<>();
    @Getter
    private final List<Door> doors = new ArrayList<>();
    @Getter
    private @NotNull RoomRotation rotation = RoomRotation.UNKNOWN;
    @Getter
    private final RoomData info;
    @Getter
    private final DataStore data = new DataStore();
    @Getter
    private RoomType type;
    public int foundSecrets = 0;
    @Getter
    private int x;
    @Getter
    private int z;
    @Getter
    private RoomState state = RoomState.UNDISCOVERED;
    @Getter
    private Vec2i arrayPos;
    @Getter
    private boolean onBloodRush = false;


    public UniqueRoom() {
        this.name = "Empty";
        Room room = new Room(0, 0);
        room.setUniqueRoom(this);
        this.mainRoom = room;
        this.rotation = RoomRotation.SOUTH;
        this.type = RoomType.NORMAL;
        this.info = new RoomData("Unknown", RoomType.UNKNOWN, RoomShape.UNKNOWN, List.of(), 0, 0, 0);
    }

    UniqueRoom(int x, int z, Room room) {
        this.name = room.getData().name();
        this.tiles.add(room);
        this.info = room.getData();
        this.arrayPos = new Vec2i(x, z);

        room.setUniqueRoom(this);
        this.scanRotation();

        DungeonInfo.cryptCount += room.getData().crypts();
        DungeonInfo.secretCount += room.getData().secrets();

        this.type = room.getData().type();

        if (this.type == RoomType.TRAP) {
            DungeonInfo.setTrapType(room.getData().name().split(" ")[0]);
        }
    }

    void addTile(int x, int z, Room tile) {
        if (tiles.stream().anyMatch(t -> t.getX() == tile.getX() && t.getZ() == tile.getZ())) return;
        tiles.add(tile);
        if (tile.getData().type() != this.type) {
            this.type = tile.getData().type(); // ???
        }
        tile.setUniqueRoom(this);
        this.scanRotation();


        arrayPos = tiles.stream()
                .min(Comparator.comparingInt(a -> a.pos.x * 1000 + a.pos.y))
                .orElseThrow().pos;
    }

    public void addDoor(Vec2i pos, DoorType type, RoomRotation rotation) {
        var existingDoor = DungeonInfo.getDoors().stream().filter(it -> it.getPosition().equals(pos)).findFirst();

        if (existingDoor.isEmpty()) {
            var newDoor = new Door(pos, type, rotation, CollectionUtils.arrayListOf(this));
            doors.add(newDoor);
            DungeonInfo.getDoors().add(newDoor);
            return;
        }

        if (!doors.contains(existingDoor.get())) doors.add(existingDoor.get());

        if (type == DoorType.ENTRANCE) existingDoor.get().setType(DoorType.ENTRANCE);
        existingDoor.get().connectedTo.add(this);
    }

    public void update() {
        if (this.rotation == RoomRotation.UNKNOWN) this.scanRotation();
        this.scanSurroundings();
    }

    private void setMainRoom(Room room) {
        this.mainRoom = room;
        this.x = room.x;
        this.z = room.z;
        new DungeonEvent.RoomScanned(this).post();
    }

    void setState(RoomState state) {
        RoomState old = this.state;
        this.state = state;
        if (state == RoomState.GREEN) {
            this.foundSecrets = this.info.secrets();
        }
        if (old != state) new DungeonEvent.StateChange(this, old, state).post();
    }

    public boolean isSecretsComplete() {
        return this.mainRoom == null || this.mainRoom.getData().secrets() == 0 || this.foundSecrets == this.mainRoom.getData().secrets();
    }

//    public boolean isOnBloodRush() {
//        return this.doors.stream().anyMatch(d -> d.getType().equals(DoorType.WITHER) || d.getType().equals(DoorType.BLOOD));
//    }

    private void scanRotation() {
        if (this.type == RoomType.FAIRY) {
            this.rotation = RoomRotation.SOUTH;
            this.setMainRoom(tiles.first);
            return;
        }

        if (this.info.shape() == RoomShape.S4x1) {
            scan4x1();
            return;
        }

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (RoomRotation rot : RoomRotation.values) {
            for (Room tile : tiles) {
                mut.set(rot.normal.x + tile.x, tile.getRoofHeight(), rot.normal.y + tile.z);
                var bl = WorldUtils.isBlock(mut, Blocks.BLUE_TERRACOTTA);
                // unloaded
                if (bl == null) continue;

                // camel
                if (bl && (this.info.shape == RoomShape.S1x1 || isCorrectClay(mut))) {
                    this.rotation = rot;
                    this.setMainRoom(tile);
                    return;
                }
            }
        }
    }

    private void scan4x1() {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (RoomRotation rot : RoomRotation.values) {
            for (Room tile : tiles) {
                mut.set(rot.fourByOne.x + tile.x, tile.getRoofHeight(), rot.fourByOne.y + tile.z);
                if (WorldUtils.isBlockOrDefault(mut, false, Blocks.BLUE_TERRACOTTA)) {
                    this.rotation = rot;
                    this.setMainRoom(tile);
                    return;
                }
            }
        }
    }

    private void scanSurroundings() {
        for (Room tile : tiles) {
            tile.scanForDoors();
        }
    }

    private boolean isCorrectClay(BlockPos pos) {
        return pos.getHorizontals().stream().allMatch(it -> WorldUtils.isBlockOrDefault(it, false, Blocks.AIR, Blocks.BLUE_TERRACOTTA, Blocks.GREEN_TERRACOTTA));
    }

    public int roof() {
        return this.mainRoom.getRoofHeight();
    }

    public int bottom() {
        return this.mainRoom.getBottom();
    }

    // rotation shit

    public Vec3 rotatePosition(Vec3 pos) {
        var x = pos.x - 0.5;
        var z = pos.z - 0.5;
        return switch (rotation) {
            case NORTH -> new Vec3(-x + 0.5, pos.y, -z + 0.5);
            case WEST -> new Vec3(z + 0.5, pos.y, -x + 0.5);
            case EAST -> new Vec3(-z + 0.5, pos.y, x + 0.5);
            default -> pos;
        };
    }

    public Vec3 unrotatePosition(Vec3 pos) {
        var x = pos.x - 0.5;
        var z = pos.z - 0.5;
        return switch (rotation) {
            case NORTH -> new Vec3(-x + 0.5, pos.y, -z + 0.5);
            case WEST -> new Vec3(-z + 0.5, pos.y, x + 0.5);
            case EAST -> new Vec3(z + 0.5, pos.y, -x + 0.5);
            default -> pos;
        };
    }

    public Vec3 unrotatePositionFixed(Vec3 pos) {
        var x = pos.x;
        var z = pos.z;
        return switch (rotation) {
            case NORTH -> new Vec3(-x, pos.y, -z);
            case WEST -> new Vec3(-z, pos.y, x);
            case EAST -> new Vec3(z, pos.y, -x);
            default -> pos;
        };
    }

    public Vec3 rotatePositionFixed(Vec3 pos) {
        var x = pos.x;
        var z = pos.z;
        return switch (rotation) {
            case NORTH -> new Vec3(-x, pos.y, -z);
            case WEST -> new Vec3(z, pos.y, -x);
            case EAST -> new Vec3(-z, pos.y, x);
            default -> pos;
        };
    }

    public Vec3 getRelativePosition(Vec3 pos) {
        return this.unrotatePosition(pos.subtract(this.x, 0.0, this.z));
    }

    public Vec3 getRealPosition(Vec3 pos) {
        return this.rotatePosition(pos).add(this.x, 0.0, this.z);
    }

    public BlockPos getRealPosition(BlockPos pos) {
        return this.rotatePosition(new Vec3(pos)).add(this.x, 0.0, this.z).toBlockPos();
    }

    public BlockPos getRelativePosition(BlockPos pos) {
        return getRelativePosition(new Vec3(pos.x + 0.5, pos.y, pos.z + 0.5)).toBlockPos();
    }

    public Vec3 getRelativePosition(Number x, Number y, Number z) {
        return new Vec3(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    public Vec3 getRealPosition(Number x, Number y, Number z) {
        return new Vec3(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }


    public Vec3 getRelativePositionFixed(Vec3 pos) {
        return this.unrotatePositionFixed(pos.subtract(this.x, 0.0, this.z));
    }

    public Vec3 getRealPositionFixed(Vec3 pos) {
        return this.rotatePositionFixed(pos).add(this.x, 0.0, this.z);
    }

    public BlockPos getRealPositionFixed(BlockPos pos) {
        return this.rotatePositionFixed(new Vec3(pos)).add(this.x, 0.0, this.z).toBlockPos();
    }

    public BlockPos getRelativePositionFixed(BlockPos pos) {
        return getRelativePositionFixed(new Vec3(pos.x, pos.y, pos.z)).toBlockPos();
    }

    public float getRelativeYaw(float yaw) {
        return switch (rotation) {
            case EAST -> RotationUtils.wrapAngleTo180(yaw - 90);
            case NORTH -> RotationUtils.wrapAngleTo180(yaw - 180);
            case WEST -> RotationUtils.wrapAngleTo180(yaw - 270);
            default -> yaw;
        };
    }

    public float getRealYaw(float yaw) {
        return switch (rotation) {
            case EAST -> RotationUtils.wrapAngleTo180(yaw + 90);
            case NORTH -> RotationUtils.wrapAngleTo180(yaw + 180);
            case WEST -> RotationUtils.wrapAngleTo180(yaw + 270);
            default -> yaw;
        };
    }

    public Rotation getRelativeYaw(Rotation rotation) {
        return new Rotation(rotation.xRot, getRelativeYaw(rotation.yRot));
    }

    public Rotation getRealYaw(Rotation rotation) {
        return new Rotation(rotation.xRot, getRelativeYaw(rotation.yRot));
    }

    public Direction getRelativeDirection(Direction direction) {
        if (direction == Direction.DOWN || direction == Direction.UP) return direction;
        return switch (rotation) {
            case EAST -> direction.getClockWise();
            case NORTH -> direction.getOpposite();
            case WEST -> direction.getCounterClockWise();
            default -> direction;
        };
    }

    public Direction getRealDirection(Direction direction) {
        if (direction == Direction.DOWN || direction == Direction.UP) return direction;

        return switch (rotation) {
            case EAST -> direction.getCounterClockWise();
            case NORTH -> direction.getOpposite();
            case WEST -> direction.getClockWise();
            default -> direction;
        };
    }

    void setOnBloodRush(boolean value) {
        if (!value && this.onBloodRush) {
            // we might have another wither door
            if (this.doors.anyMatch(door -> (door.getType() == DoorType.BLOOD || door.getType() == DoorType.WITHER) && !door.isOpened()))
                return;
        }

        this.onBloodRush = value;
    }
}
