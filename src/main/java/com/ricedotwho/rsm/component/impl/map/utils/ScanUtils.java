package com.ricedotwho.rsm.component.impl.map.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomData;
import com.ricedotwho.rsm.component.impl.map.map.Tile;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.mixins.accessor.AccessorLevelChunk;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

@UtilityClass
public class ScanUtils implements Accessor {
    private static final Gson gson = new Gson();
    private static Set<RoomData> roomList = null;
    public void init() {
        try {
            roomList = loadRoomList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<RoomData> loadRoomList() throws IOException {
        Set<RoomData> temp = gson.fromJson(new InputStreamReader(mc.getResourceManager().getResource(ResourceLocation.parse("rsm:rooms.json")).get().open()), new TypeToken<Set<RoomData>>(){}.getType());
        return temp;
    }

    public RoomData getRoomData(int x, int z) {
        return getRoomData(getCore(x, z));
    }

    public RoomData getRoomData(int hash) {
        try {
            roomList = loadRoomList();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return roomList.stream().filter(room -> room.getCores().contains(hash)).findFirst().orElse(null);
    }

    public Pair<Integer, Integer> getRoomCenter(int posX, int posZ) {
        int roomX = Math.round((posX - DungeonScanner.startX) / 32f);
        int roomZ = Math.round((posZ - DungeonScanner.startZ) / 32f);
        return new Pair<>(roomX * 32 + DungeonScanner.startX, roomZ * 32 + DungeonScanner.startZ);
    }

    public Room getRoomFromPos(int x, int z) {
        if (!((AccessorLevelChunk) mc.level.getChunk(x >> 4, z >> 4)).isLoaded()) return null;
        int max = startCorner("max");
        int min = startCorner("min");
        if (x > max || x < min) return null;
        if (z > max || z < min) return null;
        int dx = (x - DungeonScanner.startX + 15) >> 5;
        int dz = (z - DungeonScanner.startZ + 15) >> 5;
        if(dx * 2 + dz * 22 > DungeonInfo.dungeonList.length) return null;
        Tile room = DungeonInfo.dungeonList[dx * 2 + dz * 22];
        return (room instanceof Room) ? (Room)room : null;
    }

    public int startCorner(String type) {
        // currently is only f7
        switch (type) {
            case "min":
                switch(Loc.floor) {
                    case F1:
                        break;
                    case F2:
                        break;
                    case F3:
                        break;
                    case F4:
                        break;
                    case F5:
                        break;
                    case F6:
                        break;
                    case F7:
                        break;
                }
                return -200;
            case "max":
                switch(Loc.floor) {
                    case F1:
                        break;
                    case F2:
                        break;
                    case F3:
                        break;
                    case F4:
                        break;
                    case F5:
                        break;
                    case F6:
                        break;
                    case F7:
                        break;
                }
                return -10;
        }
        return -1;
    }

    public int getCore(int x, int z) {
        StringBuilder sb = new StringBuilder(150);
        ChunkAccess chunk = mc.level.getChunk(new BlockPos(x, 0, z));
        for (int y = 140; y >= 12; y--) {
            int id = Block.getId(chunk.getBlockState(new BlockPos(x, y, z)));
            if (id != 5 && id != 54 && id != 146) {
                sb.append(id);
            }
        }
        return sb.toString().hashCode();
    }
}
