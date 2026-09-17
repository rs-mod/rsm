package com.ricedotwho.rsm.managers.dungeon.map;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Floor;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.WorldUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

@Register
@UtilityClass
public class DungeonScanner implements Accessor {
    public final int ROOM_SIZE = 32;
    public final int START = -185;
    public final int BOUND = 6;
    private boolean allLoaded = false;

    private Set<RoomData> ROOMS = null;
    @Getter
    private Set<String> ROOM_NAMES = null;
    private final Map<Integer, RoomData> ROOM_DATA_CORES = new HashMap<>();
    private final Map<String, RoomData> ROOM_DATA_NAMES = new HashMap<>();

    private UniqueRoom oldRoom = null;

    private UniqueRoom currentRoom = null;
    private boolean shouldScan = false;

    private boolean outOfBounds = false;

    // TODO: does this actually stay the same always?
    private final int MAP_ID = 1024;

    @Init
    public void init() {
        try {
            ROOMS = loadRoomList();
        } catch (IOException e) {
            RSM.getLogger().error("Error while loading roomList", e);
        }

        ROOMS.forEach(data -> {
            data.cores().forEach(core -> ROOM_DATA_CORES.put(core, data));
            ROOM_DATA_NAMES.put(data.name(), data);
        });
        ClientChunkEvents.CHUNK_LOAD.register((_, _) -> shouldScan = true);
    }

    public @Nullable RoomData getRoom(String name) {
        return ROOM_DATA_NAMES.get(name);
    }

    private Set<RoomData> loadRoomList() throws IOException {
        Set<RoomData> temp = FileUtils.getGson().fromJson(new InputStreamReader(Objects.requireNonNull(DungeonScanner.class.getResourceAsStream("/assets/rsm/rooms.json"))), new TypeToken<Set<RoomData>>(){}.getType());
        if (temp != null) {
            ROOM_NAMES = temp.stream().map(RoomData::name).collect(Collectors.toSet());
        }
        return temp;
    }

    public void reset() {
        DungeonInfo.reset();
        allLoaded = false;
        oldRoom = null;
        currentRoom = null;
        shouldScan = false;
        outOfBounds = false;
    }

    public boolean shouldScan() {
        return !allLoaded && Location.getFloor() != Floor.NONE;
    }

    @SubscribeEvent
    private void updateMap(TickEvent.ClientStart event) {
        if (Dungeon.isInBoss() || !Location.getArea().is(Island.Dungeon) || !Location.getFloor().isDungeons() || mc.player == null) return;
        ProfilerFiller profiler = Profiler.get();

        if (shouldScan && DungeonScanner.shouldScan()) {
            profiler.push("Scan");
            DungeonScanner.scan();
            shouldScan = false;
            profiler.pop();
        }

        profiler.push("UniqueRoom Update");
        DungeonInfo.getUniqueRooms().forEach(UniqueRoom::update);
        profiler.pop();

        profiler.push("Update Current");
        updateCurrentRoom();
        profiler.pop();
        if (currentRoom == null) return;

        boolean fireUnique = oldRoom == null || !oldRoom.getName().equals(currentRoom.getName());

        if (fireUnique) {
            new DungeonEvent.ChangeRoom(oldRoom, currentRoom).post();
            oldRoom = currentRoom;
        }
    }

    private void updateCurrentRoom() {
        assert mc.player != null;

        // out of bounds
        outOfBounds = currentRoom != null && (mc.player.y < currentRoom.bottom() || mc.player.y > currentRoom.roof());

        Room room = DungeonInfo.getRoomFromPos((int) mc.player.position().x(), (int) mc.player.position().z());
        currentRoom = room == null ? null : room.getUniqueRoom();
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    private void onPacket(PacketEvent.MainReceivePost event, ClientboundMapItemDataPacket packet) {
        if (mc.level == null || mc.player == null || !Location.getArea().is(Island.Dungeon)) return;
        if (packet.mapId().id() != MAP_ID) return;

        if (DungeonInfo.getDungeonMap() == null) {
            DungeonInfo.setDungeonMap(MapItem.getSavedData(packet.mapId(), mc.level));
        }

        if (DungeonInfo.getDungeonMap() != null) {
            MapScanner.updateMap(DungeonInfo.getDungeonMap());
        }
    }

    @SubscribeEvent
    private void bossEntered(DungeonEvent.EnterBoss event) {
        currentRoom = null;
        oldRoom = null;
    }

    // TODO: if a room hasn't been scanned it will not work
    public void parseSocketData(JsonObject obj) {
        // Old odin socket
        if (obj.has("roomName")) {
            var name = obj.get("roomName").getAsString();
            int foundSecrets = obj.getOrDefault("foundSecrets", -1);
            if (foundSecrets == -1) return;
            var room = DungeonInfo.getUnique(name);
            if (room == null || room.getState() == RoomState.UNDISCOVERED) return;
            room.foundSecrets = foundSecrets;
        } else {
            JsonObject data = obj.getAsJsonObject("data");
            if (data == null || !data.has("name") || !obj.has("foundSecrets")) return;
            var name = data.get("name").getAsString();
            var room = DungeonInfo.getUnique(name);
            if (room == null || room.getState() == RoomState.UNDISCOVERED) return;
            room.foundSecrets = obj.get("foundSecrets").getAsInt();
        }
    }


    public void scan() {
        ProfilerFiller profiler = Profiler.get();
        boolean allChunksLoaded = true;
        boolean notNull = true;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        assert mc.level != null;

        profiler.push("find chunk");


        for (int x = 0; x < BOUND; x++) {
            for (int z = 0; z < BOUND; z++) {
                int index = z * BOUND + x;

                // this room has already been added in a previous scan.
                if (DungeonInfo.dungeonRooms[index] != null) {
                    continue;
                }

                profiler.push("find chunk 2");
                int xPos = START + x * ROOM_SIZE;
                int zPos = START + z * ROOM_SIZE;
                mutable.set(xPos, 67, zPos);

                if (!mc.level.isLoaded(mutable)) {
                    allChunksLoaded = false;
                    profiler.pop();
                    continue;
                }

                Room result = scanTile(x, z);
                if (result != null) {
                    DungeonInfo.dungeonRooms[index] = result;
                } else {
                    notNull = false;
                }
                profiler.pop();
            }
        }

        profiler.popPush("check done");

        if (notNull && allChunksLoaded && DungeonInfo.getUniqueRooms().stream().noneMatch(r  -> r.getRotation().equals(RoomRotation.UNKNOWN))) {
            DungeonInfo.setRoomCount(DungeonInfo.getUniqueRooms().size());
            allLoaded = true;
            new DungeonEvent.ScanComplete().post();
        }
        profiler.pop();
    }

    private Room scanTile(int x, int z) {
        int rx = START + x * ROOM_SIZE;
        int rz = START + z * ROOM_SIZE;

        var chunk = WorldUtils.getChunkOrNull(rx >> 4, rz >> 4);
        if (chunk == null) return null;
        var roof = getRoofHeight(rx, rz, chunk);
        var core = getCore(rx, rz, roof, chunk);

        // unloaded? it's spamming my logs
        if (core == -318865360) return null;

        var data = ROOM_DATA_CORES.get(core);
        if (data == null) {
            RSM.getLogger().warn("RoomData is null for {} at x: {}, z: {}", core, rx, rz);
            return null;
        }

        var bottom = getRoomBottom(rx, rz, chunk);

        var room = new Room(rx, rz, x, z, roof, bottom, data);
        room.addToUnique(x, z);

        return room;
    }

    public int getCore(int x, int z, int roomHeight, ChunkAccess chunk) {
        assert mc.level != null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        StringBuilder sb = new StringBuilder(150);
        int clampedHeight = Math.clamp(roomHeight, 11, 140);

        sb.repeat("0", 140 - clampedHeight);

        int bedrock = 0;

        for (int y = clampedHeight; y >= 12; y--) {
            mutableBlockPos.set(x, y, z);
            Block block = chunk.getBlockState(mutableBlockPos).getBlock();
            if (block == Blocks.AIR && bedrock >= 2 && y < 69) {
                sb.repeat("0", y - 11);
                break;
            }

            if (block == Blocks.BEDROCK) {
                bedrock++;
            } else {
                bedrock = 0;
                if (Utils.equalsOneOf(block,
                        Blocks.OAK_PLANKS,
                        Blocks.TRAPPED_CHEST,
                        Blocks.CHEST)) {
                    continue;
                }
            }
            sb.append(block);
        }
        return sb.toString().hashCode();
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
        return -1;
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

    @ApiStatus.Internal
    public UniqueRoom currentRoom() {
        return outOfBounds ? null : currentRoom;
    }
}
