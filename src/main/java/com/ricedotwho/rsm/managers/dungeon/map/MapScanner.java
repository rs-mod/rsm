package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.type.Vec2i;
import com.ricedotwho.rsm.utils.ChatUtils;
import kotlin.UIntKt;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Arrays;

@UtilityClass
class MapScanner {
    private Vec2i startCoords = null;
    private int roomSize = 0;

    void updateMap(MapItemSavedData mapData) {
        var colors = mapData.colors;

        if (startCoords == null && !initializeSizes(colors)) return;

        scanRooms(colors);
        scanDoors(colors);
    }

    void scanRooms(byte[] colors) {
        Vec2i sc = startCoords.add(roomSize / 2);
        var tile = roomSize + 4;


        DungeonInfo.getUniqueRooms().forEach(room -> {
            Vec2i placement = room.getArrayPos();

            byte color = placementColor(placement, sc, colors, tile);

            if (color == 0) {
                Vec2i newPlacement = null;
                byte newColor = 0;

                for (Room place : room.getTiles()) {
                    byte candidateColor = placementColor(place.pos, sc, colors, tile);

                    if (candidateColor != 0) {
                        newPlacement = place.pos;
                        newColor = candidateColor;
                        break;
                    }
                }

                if (newPlacement != null) {
                    color = newColor;
                }
            }
            room.setState(MapScanner.getRoomState(color, room.getType(), room));
        });
    }

    byte placementColor(Vec2i placement, Vec2i sc, byte[] colors, int tile) {
        Vec2i center = sc.add(placement.multiply(tile));
        int mapIndex = center.y * 128 + center.x;

        if (colors.length <= mapIndex) {
            return (byte) 0;
        }

        return colors[mapIndex];
    }

    void scanDoors(byte[] colors) {
        if (roomSize == 0) return;
        var hrs = roomSize / 2;
        var sc = startCoords.add(hrs, hrs);

        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 6; b++) {
                var door = hrs + a * (roomSize + 4);
                var midRoom = b * (roomSize + 4);

                var coordsDoor = DungeonScanner.START + 16 + (a * 32);
                var coordsMidRoom = DungeonScanner.START + (b * 32);

                // the room index is the index of the gap between 2 rooms, if there's a door the gap has 0 as a color
                // we need that to make sure that we're looking at a door and not just a connection between tiles in
                // a bigger room
                var doorIndex = mapIndex(sc.add(door, midRoom));
                var roomIndex = mapIndex(sc.add(door, midRoom - hrs + 1));
                if (colors.length > doorIndex && colors[roomIndex] == 0) {
                    handleDoor(new Vec2i(coordsDoor, coordsMidRoom), colors[doorIndex]);
                }

                var doorIndex2 = mapIndex(sc.add(midRoom, door));
                var roomIndex2 = mapIndex(sc.add(midRoom - hrs + 1, door));
                if (colors.length > doorIndex2 && colors[roomIndex2] == 0) {
                    handleDoor(new Vec2i(coordsMidRoom, coordsDoor), colors[doorIndex2]);
                }
            }
        }
    }

    void handleDoor(Vec2i pos, byte colour) {
        DoorType type;
        switch (colour) {
            // locked wither or locked fairy, fairy
            case 119, 82 -> type = DoorType.WITHER;
            // normal, fairy, puzzle, normal but unopened, trap, champion
            case 63, 66, 85, 62, 74 -> type = DoorType.NORMAL;
            // blood, stays the same color even when unlocked
            case 18 -> type = DoorType.BLOOD;
            // if it's not on the map no point in creating a door
            case 0 -> {
                return;
            }
            // I don't think there's any other colours that we're supposed to handle
            default -> throw new IllegalStateException("found a color for a door that doesn't exist: ${colour}");
        }

        DungeonInfo.getDoors().filter(it -> it.getPosition().x() == pos.x() && it.getPosition().y() == pos.y()).findFirst().ifPresent(door -> {
            if (type == DoorType.WITHER && door.getType() != DoorType.WITHER) {
                door.setType(DoorType.WITHER);
                door.setOpened(false);
            }

            if (type == DoorType.WITHER || type == DoorType.BLOOD) {
                door.connectedTo.forEach(it -> it.setOnBloodRush(true));
            }

            if (type == DoorType.NORMAL || colour == 82) {
                door.setOpened(true);
            }

        if (colour == 18) {
            door.setOpened(Dungeon.isBloodOpen());
        }
        });
    }

    int mapIndex(Vec2i vec2i) {
        return vec2i.y * 128 + vec2i.x();
    }

    private boolean initializeSizes(byte[] colors) {
        int greenStart = -1;
        int greenLength = 0;

        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == 30) {
                if (greenLength++ == 0) {
                    greenStart = i;
                }
            } else {
                if (greenLength >= 16) {
                    break;
                }
                greenLength = 0;
            }
        }

        if (greenLength != 16 && greenLength != 18) {
            return false;
        }

        Vec2i start;

        switch (Location.getFloor().getNumber()) {
            case 0 -> start = new Vec2i(22, 22);
            case 1 -> start = new Vec2i(22, 11);
            case 2, 3 -> start = new Vec2i(11, 11);
            default -> start = new Vec2i(
                    (greenStart & 127) % (greenLength + 4),
                    (greenStart >> 7) % (greenLength + 4)
            );
        }
        roomSize = greenLength;
        startCoords = start;
        return true;
    }

    RoomState getRoomState(int centerColor, RoomType type, UniqueRoom room) {
        RoomState state;
        if (centerColor == 18) {
            if (type == RoomType.BLOOD) {
                state = RoomState.DISCOVERED;
            } else if (type == RoomType.PUZZLE) {
                state = RoomState.FAILED;
            } else {
                state = room.getState();
            }
        } else if (centerColor == 30) {
            if (type == RoomType.ENTRANCE) {
                state = RoomState.DISCOVERED;
            } else {
                state = RoomState.GREEN;
            }
        } else if (centerColor == 34) {
            state = RoomState.CLEARED;
        } else if (centerColor == 85 || centerColor == 119) {
            state = RoomState.UNOPENED;
        } else if (centerColor == 0) {
            state = RoomState.UNDISCOVERED;
        } else {
            state = RoomState.DISCOVERED;
        }
        return state;
    }
}
