package com.ricedotwho.rsm.component.impl.map.utils;

import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonMapColorParser;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.Utils;
import lombok.experimental.UtilityClass;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@UtilityClass
public class MapUtils implements Accessor {
    public static Pair<Integer, Integer> startCorner = new Pair<>(5, 5);
    public static int mapRoomSize = 16;
    public static double coordMultiplier = 0.625;
    public static boolean calibrated = false;

    public MapItemSavedData getMapData() {
        ItemStack map = mc.player != null ? mc.player.getInventory().getItem(8) : null;
        if (map == null || !(map.getItem() instanceof MapItem) || !map.getDisplayName().getString().contains("Magical Map")) {
            return null;
        }
        return MapItem.getSavedData(map, mc.level);
    }

    /**
     * Calibrates map metrics based on the size and location of the entrance room.
     */
    public boolean calibrateMap() {
        Pair<Integer, Integer> entrance = findEntranceCorner();
        if(entrance == null) return false;
        int start = entrance.getFirst();
        int size = entrance.getSecond();
        if (Utils.equalsOneOf(size, 16, 18)) {
            mapRoomSize = size;
            switch (Loc.getFloor()) {
                case F1:
                    startCorner = new Pair<>(22, 22);
                    break;
                case F2:
                    startCorner = new Pair<>(22, 11);
                    break;
                case F3:
                case F4:
                    startCorner = new Pair<>(11, 11);
                    break;
                default:
                    int startX = start & 127;
                    int startZ = start >> 7;
                    startCorner = new Pair<>(startX % (mapRoomSize + 4), startZ % (mapRoomSize + 4));
                    break;
            }
            coordMultiplier = (mapRoomSize + 4.0) / DungeonScanner.roomSize;

            DungeonMapColorParser.calibrate();
            return true;
        }
        return false;
    }

    /**
     * Finds the starting index of the entrance room as well as the size of the room.
     */
    private Pair<Integer, Integer> findEntranceCorner() {
        int start = 0;
        int currLength = 0;
        byte[] colors;
        if (DungeonInfo.dungeonMap != null) {
            colors = DungeonInfo.dungeonMap.colors;
        } else if (DungeonInfo.guessMapData != null) {
            colors = DungeonInfo.guessMapData.colors;
        } else {
            return null;
        }
        for (int index = 0; index < colors.length; index++) {
            byte color = colors[index];
            if (color == 30) {
                if (currLength == 0) start = index;
                currLength++;
            } else {
                if (currLength >= 16) {
                    return new Pair<>(start, currLength);
                }
                currLength = 0;
            }
        }
        return new Pair<>(start, currLength);
    }
}

