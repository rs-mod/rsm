package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.impl.map.MapElement;
import com.ricedotwho.rsm.component.impl.map.map.*;
import com.ricedotwho.rsm.mixins.accessor.AccessorLevelChunk;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

public class MapUpdater implements Accessor {
    public static void updateRooms(MapItemSavedData mapData) {
        DungeonMapColorParser.updateMap(mapData);
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                Tile room = DungeonInfo.dungeonList[z * 11 + x];
                Tile mapTile = DungeonMapColorParser.getTile(x, z);
                if(mapTile == null) continue;


                if(room instanceof Room) {
                    Room room1 = (Room) room;
                    int centerColour = DungeonMapColorParser.getCenterColour(x, z);
                    room1.setState(DungeonMapColorParser.getRoomState(centerColour, room1.getData().getType(), room1));
                }

                if (room instanceof Unknown) {
                    DungeonInfo.dungeonList[z * 11 + x] = mapTile;
                    if (mapTile instanceof Room) {
                        Room roomTile = (Room) mapTile;
                        List<Room> connected = DungeonMapColorParser.getConnected(x, z);

                        for (Room connectedRoom : connected) {
                            if (!"Unknown".equals(connectedRoom.getData().getName())) {
                                roomTile.addToUnique(z, x, connectedRoom.getData().getName());
                                break;
                            }
                        }
                    }
                    continue;
                }

                if (mapTile instanceof Door && room instanceof Door) {
                    Door doorMapTile = (Door) mapTile;
                    Door doorRoom = (Door) room;

                    doorRoom.setState(MapElement.getDoorState(doorRoom, x, z));

                    if (doorMapTile.getType() == DoorType.WITHER && doorRoom.getType() != DoorType.WITHER) {
                        doorRoom.setType(doorMapTile.getType());
                    }
                }

                if (room instanceof Door) {
                    Door doorRoom = (Door) room;

                    if (Utils.equalsOneOf(doorRoom.getType(), DoorType.ENTRANCE, DoorType.WITHER, DoorType.BLOOD)) {
                        if (mapTile instanceof Door && ((Door) mapTile).getType() == DoorType.WITHER) {
                            doorRoom.setOpened(false);
                        } else if (!doorRoom.isOpened()) {
                            LevelChunk chunk = mc.level.getChunk(
                                    doorRoom.getX() >> 4,
                                    doorRoom.getZ() >> 4
                            );

                            if (((AccessorLevelChunk) chunk).isLoaded()) {
                                if (chunk.getBlockState(new BlockPos(doorRoom.getX(), 69, doorRoom.getZ())).getBlock() == Blocks.AIR) {
                                    doorRoom.setOpened(true);
                                }
                            } else if (mapTile instanceof Door && mapTile.getState() == RoomState.DISCOVERED) {
                                if (doorRoom.getType() == DoorType.BLOOD) {

                                    UniqueRoom bloodRoom = DungeonInfo.getRoomByName("Blood");

                                    if (bloodRoom != null && bloodRoom.getMainRoom() != null && bloodRoom.getMainRoom().getState() != RoomState.UNOPENED) {
                                        if (bloodRoom.getMainRoom().getData().getType() == RoomType.BLOOD) {
                                            doorRoom.setOpened(true);
                                        }
                                    }
                                } else {
                                    doorRoom.setOpened(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
