package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.impl.map.MapElement;
import com.ricedotwho.rsm.component.impl.map.map.*;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.Utils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

@UtilityClass
public class MapUpdater implements Accessor {
    public void updateRooms(MapItemSavedData mapData) {
        DungeonMapColorParser.updateMap(mapData);
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                Tile room = DungeonInfo.getDungeonList()[z * 11 + x];
                Tile mapTile = DungeonMapColorParser.getTile(x, z);
                if(mapTile == null) continue;

                if(room instanceof Room room1) {
                    int centerColour = DungeonMapColorParser.getCenterColour(x, z);
                    room1.setState(DungeonMapColorParser.getRoomState(centerColour, room1.getData().type(), room1));
                }

                if (room instanceof Unknown) {
                    DungeonInfo.getDungeonList()[z * 11 + x] = mapTile;
                    if (mapTile instanceof Room roomTile) {
                        List<Room> connected = DungeonMapColorParser.getConnected(x, z);

                        for (Room connectedRoom : connected) {
                            if (!"Unknown".equals(connectedRoom.getData().name())) {
                                roomTile.addToUnique(z, x, connectedRoom.getData().name());
                                break;
                            }
                        }
                    }
                    continue;
                }

                if (mapTile instanceof Door doorMapTile && room instanceof Door doorRoom) {

                    doorRoom.setState(MapElement.getDoorState(doorRoom, x, z));

                    if (doorMapTile.getType() == DoorType.WITHER && doorRoom.getType() != DoorType.WITHER) {
                        doorRoom.setType(doorMapTile.getType());
                    }
                }

                if (room instanceof Door doorRoom) {

                    if (Utils.equalsOneOf(doorRoom.getType(), DoorType.ENTRANCE, DoorType.WITHER, DoorType.BLOOD)) {
                        if (mapTile instanceof Door && ((Door) mapTile).getType() == DoorType.WITHER) {
                            doorRoom.setOpened(false);
                        } else if (!doorRoom.isOpened()) {
                            assert mc.level != null;
                            LevelChunk chunk = mc.level.getChunk(
                                    doorRoom.getX() >> 4,
                                    doorRoom.getZ() >> 4
                            );

                            assert Minecraft.getInstance().level != null;
                            if (Minecraft.getInstance().level.isLoaded(new BlockPos(doorRoom.getX(), 67, doorRoom.getZ()))) {
                                if (chunk.getBlockState(new BlockPos(doorRoom.getX(), 69, doorRoom.getZ())).getBlock() == Blocks.AIR) {
                                    doorRoom.setOpened(true);
                                }
                            } else if (mapTile instanceof Door && mapTile.getState() == RoomState.DISCOVERED) {
                                if (doorRoom.getType() == DoorType.BLOOD) {

                                    UniqueRoom bloodRoom = DungeonInfo.getRoomByName("Blood");

                                    if (bloodRoom != null && bloodRoom.getMainRoom() != null && bloodRoom.getMainRoom().getState() != RoomState.UNOPENED) {
                                        if (bloodRoom.getMainRoom().getData().type() == RoomType.BLOOD) {
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
