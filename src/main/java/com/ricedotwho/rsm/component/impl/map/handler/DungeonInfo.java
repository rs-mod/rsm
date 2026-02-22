package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.impl.map.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.*;

@UtilityClass
public class DungeonInfo {
    // 6 x 6 room grid, 11 x 11 with connections
    @Getter
    private final Tile[] dungeonList = new Tile[121];
    @Getter
    private final Set<Room> roomList = new HashSet<>();
    @Getter
    private final Set<Door> doorList = new HashSet<>();
    @Getter
    private final Set<UniqueRoom> uniqueRooms = new HashSet<>();
    @Setter
    @Getter
    private int roomCount = 0;
    @Getter
    private final Map<Puzzle, Boolean> puzzles = new HashMap<>();
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

    static {
        Arrays.fill(dungeonList, new Unknown(0, 0));
    }

    public void reset() {
        resetRooms();
        roomCount = 0;
        uniqueRooms.clear();
        puzzles.clear();
        doorList.clear();

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
            if(name.equalsIgnoreCase(unique.getName())) {
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

