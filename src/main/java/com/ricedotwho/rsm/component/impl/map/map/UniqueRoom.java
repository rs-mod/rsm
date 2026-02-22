package com.ricedotwho.rsm.component.impl.map.map;

import com.ricedotwho.rsm.component.impl.map.MapElement;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.DataStore;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class UniqueRoom {
    boolean centerNames = true;
    boolean centerCheck = true;
    @Getter
    private final String name;
    private Pair<Integer, Integer> topLeft;
    private Pair<Integer, Integer> center;
    @Getter
    private Room mainRoom;
    @Getter
    private Room topLeftRoom;
    @Getter
    private final List<Room> tiles = new ArrayList<>();
    @Getter
    private final List<Door> doors = new ArrayList<>();
    @Setter
    @Getter
    private RoomRotation rotation;
    @Getter
    private final DataStore data = new DataStore();

    private static final List<Pair<Integer, Integer>> DOOR_OFFSETS = List.of(
            new Pair<>(0, 16),
            new Pair<>(0, -16),
            new Pair<>(-16, 0),
            new Pair<>(16, 0)
    );

    private UniqueRoom() {
        this.name = "Empty";
        Room room = Room.emptyRoom();
        room.setUniqueRoom(this);
        this.mainRoom = room;
        this.topLeftRoom = room;
        this.rotation = RoomRotation.TOPLEFT;
    }

    public UniqueRoom(int arrX, int arrY, Room room) {
        this.name = room.getData().name();
        this.topLeft = new Pair<>(arrX, arrY);
        this.center = new Pair<>(arrX, arrY);
        this.topLeftRoom = room;
        this.tiles.add(room);

        room.setUniqueRoom(this);
        RoomUtils.findMainAndRotation(this);

        DungeonInfo.cryptCount += room.getData().crypts();
        DungeonInfo.secretCount += room.getData().secrets();

        switch (room.getData().type()) {
            case ENTRANCE:
                MapElement.dynamicRotation = (arrY == 0) ? 180f : ((arrX == 0) ? -90f : (arrX > arrY) ? 90f : 0f);
                break;

            case TRAP:
                DungeonInfo.setTrapType(room.getData().name().split(" ")[0]);
                break;

            default:
                break;
        }
    }

    public void addTile(int x, int z, Room tile) {
        if (tiles.stream().anyMatch(t -> t.getX() == tile.getX() && t.getZ() == tile.getZ())) return;
        tiles.add(tile);
        tile.setUniqueRoom(this);
        RoomUtils.findMainAndRotation(this);

        if (x < topLeft.getFirst() || (x == topLeft.getFirst() && z < topLeft.getSecond())) {
            topLeft = new Pair<>(x, z);
            topLeftRoom = tile;
        }

        // doors
        findDoors(x, z);

        if (tiles.size() == 1) {
            center = new Pair<>(x, z);
            return;
        }

        List<Pair<Integer, Integer>> positions = new ArrayList<>();
        for (Room t : tiles) {
            Pair<Integer, Integer> arrPos = t.getArrayPosition();
            if (arrPos.getFirst() % 2 == 0 && arrPos.getSecond() % 2 == 0) {
                positions.add(arrPos);
            }
        }

        if (positions.isEmpty()) return;

        Map<Integer, List<Integer>> xRooms = positions.stream()
                .collect(Collectors.groupingBy(Pair::getFirst, Collectors.mapping(Pair::getSecond, Collectors.toList())));
        Map<Integer, List<Integer>> zRooms = positions.stream()
                .collect(Collectors.groupingBy(Pair::getSecond, Collectors.mapping(Pair::getFirst, Collectors.toList())));

        if (zRooms.size() == 1 || zRooms.entrySet().stream().max(Comparator.comparingInt(e -> e.getValue().size())).get().getValue().size() != zRooms.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).skip(1).findFirst().get().getValue().size()) {
            center = new Pair<>(xRooms.keySet().stream().mapToInt(i -> i).sum() / xRooms.size(), zRooms.entrySet().stream().findFirst().get().getKey());
        } else if (xRooms.size() == 1 || xRooms.entrySet().stream().max(Comparator.comparingInt(e -> e.getValue().size())).get().getValue().size() != xRooms.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).skip(1).findFirst().get().getValue().size()) {
            center = new Pair<>(xRooms.entrySet().stream().findFirst().get().getKey(), zRooms.keySet().stream().mapToInt(i -> i).sum() / zRooms.size());
        } else {
            int xCenter = (int) Math.round((xRooms.keySet().stream().mapToInt(i -> i).sum() + xRooms.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).skip(1).findFirst().get().getKey()) / 2.0);
            int zCenter = (int) Math.round((zRooms.keySet().stream().mapToInt(i -> i).sum() + zRooms.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).skip(1).findFirst().get().getKey()) / 2.0);
            center = new Pair<>(xCenter, zCenter);
        }
    }

    private void findDoors(int rx, int rz) {
        for (Pair<Integer, Integer> pair : DOOR_OFFSETS) {
            int x = rx + pair.getFirst();
            int z = rz + pair.getSecond();
            Door door = ScanUtils.getDoorFromPos(x, z);
            if (door != null) {
                addDoor(door);
            }
        }
    }

    public void addDoor(Door door) {
        Optional<Door> prior = doors.stream().filter(d -> d.getX() == door.getX() && d.getZ() == door.getZ()).findFirst();
        prior.ifPresent(this.doors::remove);
        this.doors.add(door);
    }

    public void update() {
        if (!Utils.equalsOneOf(this.rotation, RoomRotation.UNKNOWN, null)) return;
        RoomUtils.findMainAndRotation(this);
    }

    public Pair<Integer, Integer> getNamePosition() {
        return centerNames ? center : topLeft;
    }

    public Pair<Integer, Integer> getCheckmarkPosition() {
        return centerCheck ? center : topLeft;
    }

    public void setMainRoom(Room room) {
        mainRoom = room;
        new DungeonEvent.RoomScanned(this).post();
    }

    public boolean isOnBloodRush() {
        return this.doors.stream().anyMatch(d -> d.getType().equals(DoorType.WITHER) || d.getType().equals(DoorType.BLOOD));
    }

    public static UniqueRoom emptyUnique() {
        return new UniqueRoom();
    }
}

