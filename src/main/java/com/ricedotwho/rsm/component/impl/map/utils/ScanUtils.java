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
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.Utils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
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
        return gson.fromJson(new InputStreamReader(Objects.requireNonNull(ScanUtils.class.getResourceAsStream("/assets/rsm/rooms.json"))), new TypeToken<Set<RoomData>>(){}.getType());
    }

    public RoomData getRoomData(int hash) {
        try {
            roomList = loadRoomList();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return roomList.stream().filter(room -> room.cores().contains(hash)).findFirst().orElse(null);
    }

    public Pair<Integer, Integer> getRoomCenter(int posX, int posZ) {
        int roomX = Math.round((posX - DungeonScanner.startX) / 32f);
        int roomZ = Math.round((posZ - DungeonScanner.startZ) / 32f);
        return new Pair<>(roomX * 32 + DungeonScanner.startX, roomZ * 32 + DungeonScanner.startZ);
    }

    public Room getRoomFromPos(int x, int z) {
        assert Minecraft.getInstance().level != null;
        if (!Minecraft.getInstance().level.isLoaded(new BlockPos(x, 67, z))) return null;
        int max = startCorner("max");
        int min = startCorner("min");
        if (x > max || x < min) return null;
        if (z > max || z < min) return null;
        int dx = (x - DungeonScanner.startX + 15) >> 5;
        int dz = (z - DungeonScanner.startZ + 15) >> 5;
        if(dx * 2 + dz * 22 > DungeonInfo.getDungeonList().length) return null;
        Tile room = DungeonInfo.getDungeonList()[dx * 2 + dz * 22];
        return (room instanceof Room) ? (Room)room : null;
    }

    public int startCorner(String type) {
        // currently is only f7
        switch (type) {
            case "min":
                switch(Loc.getFloor()) {
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
                switch(Loc.getFloor()) {
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

    public int getCore(int x, int z, int roomHeight, ChunkAccess chunk) {
        assert mc.level != null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        StringBuilder sb = new StringBuilder(150);
        int clampedHeight = Math.max(11, Math.min(roomHeight, 140));

        sb.append("0".repeat(140 - clampedHeight));

        int bedrock = 0;

        for (int y = clampedHeight; y >= 12; y--) {
            mutableBlockPos.set(x, y, z);
            Block block = chunk.getBlockState(mutableBlockPos).getBlock();
            if (block == Blocks.AIR && bedrock >= 2 && y < 69) {
                sb.append("0".repeat(y - 11));
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
}
