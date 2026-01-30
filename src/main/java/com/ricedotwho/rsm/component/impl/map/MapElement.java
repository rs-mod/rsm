package com.ricedotwho.rsm.component.impl.map;

import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.map.*;
import com.ricedotwho.rsm.data.Pair;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapElement {
    public static float dynamicRotation = 0f;

    /**
     * Gets the state of a door based on its connecting rooms.
     *
     * @param door The door to check
     * @param row The row position of the door
     * @param column The column position of the door
     * @return The calculated room state for the door
     */
    public RoomState getDoorState(Door door, int row, int column) {
        Pair<Room, Room> rooms = getConnectingRooms(row, column);

        if (rooms == null) {
            return RoomState.UNDISCOVERED;
        }

        for (Object obj : rooms.toList()) {
            Room room = (Room) obj;
            if(RoomType.FAIRY.equals(room.getData().getType())) {
                door.setType(DoorType.WITHER);
            }
            if (room.getState() == RoomState.DISCOVERED) {
                return RoomState.DISCOVERED;
            }
        }
        return RoomState.UNDISCOVERED;
    }
    private Pair<Room, Room> getConnectingRooms(int row, int column) {
        boolean vertical = column % 2 == 0;

        Pair<Tile, Tile> connectingTiles;
        try {
            Tile firstTile;
            Tile secondTile;
            if (vertical) {
                firstTile = DungeonInfo.dungeonList[(row - 1) * 11 + column];
                secondTile = DungeonInfo.dungeonList[(row + 1) * 11 + column];
            } else {
                firstTile = DungeonInfo.dungeonList[row * 11 + column - 1];
                secondTile = DungeonInfo.dungeonList[row * 11 + column + 1];
            }
            connectingTiles = new Pair<>(firstTile, secondTile);
        } catch (Exception e) {
            return null;
        }

        if (!(connectingTiles.getFirst() instanceof Room) || !(connectingTiles.getSecond() instanceof Room)) {
            return null;
        }

        return new Pair<>((Room) connectingTiles.getFirst(), (Room) connectingTiles.getSecond());
    }
}
