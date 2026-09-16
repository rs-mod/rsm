package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.type.Vec2i;
import lombok.experimental.UtilityClass;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Comparator;
import java.util.function.Function;

@UtilityClass
class MapScanner {
    private Vec2i startCoords = null;
    private int roomSize = 0;

    void updateMap(MapItemSavedData mapData) {
        var colors = mapData.colors;

        if (startCoords == null && !initializeSizes(colors)) return;
        Vec2i sc = startCoords.add(roomSize / 2);
        var tile = roomSize + 4;


        DungeonInfo.getUniqueRooms().forEach(room -> {

            Function<Vec2i, Byte> placementColor = placement -> {
                Vec2i center = sc.add(placement.multiply(tile));
                int mapIndex = center.y * 128 + center.x;

                if (colors.length <= mapIndex) {
                    return (byte) 0;
                }

                return colors[mapIndex];
            };

            Vec2i placement = room.getTiles().stream()
                    .min(Comparator.comparingInt(a -> a.pos.x * 1000 + a.pos.y))
                    .orElseThrow().pos;

            byte color = placementColor.apply(placement);

            if (color == 0) {
                Vec2i newPlacement = null;
                byte newColor = 0;

                for (Room place : room.getTiles()) {
                    byte candidateColor = placementColor.apply(place.pos);

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
