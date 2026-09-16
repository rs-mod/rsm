package com.ricedotwho.rsm.managers.dungeon.map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;

import java.util.*;

@UtilityClass
public class DungeonInfo {
    @Getter
    final Room[] dungeonRooms = new Room[36];
    @Getter
    private final Set<Door> doors = new HashSet<>();
    @Getter
    private final Set<UniqueRoom> uniqueRooms = new HashSet<>();
    @Setter
    @Getter
    private int roomCount = 0;
    @Setter
    @Getter
    private String trapType = "";
    @Setter
    @Getter
    public int witherDoors = 0;
    public int cryptCount = 0;
    public int secretCount = 0;
    public int keys = 0;
    @Setter
    @Getter
    private MapItemSavedData dungeonMap = null;
    @Setter
    @Getter
    private MapItemSavedData guessMapData = null;
    private final Map<String, UniqueRoom> NAME_TO_UNIQUE = new HashMap<>();

    static {
        Arrays.fill(dungeonRooms, null);
    }

    public void reset() {
        resetRooms();
        roomCount = 0;
        uniqueRooms.clear();
        doors.clear();
        NAME_TO_UNIQUE.clear();

        trapType = "";
        witherDoors = 0;
        cryptCount = 0;
        secretCount = 0;

        keys = 0;

        dungeonMap = null;
        guessMapData = null;
    }

    public void resetRooms() {
        Arrays.fill(dungeonRooms, null);
    }

    public void addUnique(UniqueRoom uni) {
        uniqueRooms.add(uni);
        NAME_TO_UNIQUE.put(uni.getName(), uni);
    }

    public @Nullable UniqueRoom getUnique(String name) {
        return NAME_TO_UNIQUE.get(name);
    }

    Room getRoomFromPos(int x, int z) {
        var dx = ((x - DungeonScanner.START + 15) >> 5);
        var dz = ((z - DungeonScanner.START + 15) >> 5);
        var index = dx + dz * DungeonScanner.BOUND;
        if (index < 0 || index >= DungeonScanner.BOUND * DungeonScanner.BOUND) return null;
        return DungeonInfo.dungeonRooms[index];
    }

    public UniqueRoom getRoomFromPos0(int x, int z) {
        Room room = getRoomFromPos(x, z);
        return room == null ? null : room.getUniqueRoom();
    }
}
