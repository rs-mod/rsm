package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.impl.map.map.*;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.*;

@UtilityClass
public class DungeonInfo {
    // 6 x 6 room grid, 11 x 11 with connections
    public static final Tile[] dungeonList = new Tile[121];
    public static List<Room> roomList = new ArrayList<>();
    public static final Set<UniqueRoom> uniqueRooms = new HashSet<>();
    public static int roomCount = 0;
    public static final Map<Puzzle, Boolean> puzzles = new HashMap<>();

    public static String trapType = "";
    public static int witherDoors = 0;
    public static int cryptCount = 0;
    public static int secretCount = 0;

    public static int keys = 0;

    public static MapItemSavedData dungeonMap = null;
    public static MapItemSavedData guessMapData = null;

    static {
        Arrays.fill(dungeonList, new Unknown(0, 0));
    }

    public void reset() {
        resetRooms();
        roomCount = 0;
        uniqueRooms.clear();
        puzzles.clear();

        trapType = "";
        witherDoors = 0;
        cryptCount = 0;
        secretCount = 0;

        keys = 0;

        dungeonMap = null;
        guessMapData = null;
    }

    public void resetRooms() {
        Arrays.fill(dungeonList, new Unknown(0, 0));
    }

    public UniqueRoom getRoomByName(String name) {
        for (UniqueRoom unique : uniqueRooms) {
            if(name.equals(unique.getName())) {
                return unique;
            }
        }
        return null;
    }

    public Door findNextDoor() {
        for (Tile t : dungeonList) {
            if (!(t instanceof Door door)) continue;
            if (door.isOpened() || door.getType() == DoorType.NORMAL || door.getState() != RoomState.DISCOVERED) continue;
            return door;
        }
        return null;
    }
}

