package com.ricedotwho.rsm.component.impl.map;

import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.component.impl.map.handler.*;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.MapUtils;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.task.ScheduledTask;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.WorldEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.thread.TaskScheduler;

import java.util.Objects;

public class Map extends ModComponent {
    public Room oldRoom = null;

    public Map() {
        super("Map");
    }

    public static void reset() {
        RoomUtils.reset();
        DungeonInfo.reset();
        MapUtils.calibrated = false;
        DungeonScanner.hasScanned = false;
        Dungeon.dungeonStarted = false;
        Dungeon.inBoss = false;
    }

    @SubscribeEvent
    public void updateMap(ClientTickEvent.Start event) {
        if (Dungeon.inBoss || !Loc.area.is(Island.Dungeon) || mc.player == null) return;

        if (!MapUtils.calibrated) {
            if (DungeonInfo.dungeonMap == null) {
                DungeonInfo.dungeonMap = MapUtils.getMapData();
            }

            MapUtils.calibrated = MapUtils.calibrateMap();
        } else {
            if (DungeonInfo.dungeonMap != null) MapUpdater.updateRooms(DungeonInfo.dungeonMap);
            DungeonInfo.uniqueRooms.forEach(UniqueRoom::update);
        }

        if (DungeonScanner.shouldScan()) {
            DungeonScanner.scan();
        }

        RoomUtils.getCurrentRoom();
        if (RoomUtils.playerCurrentRoom == null) return;
        if (oldRoom == null) {
            oldRoom = RoomUtils.playerCurrentRoom;
        }


        if (!Objects.equals(RoomUtils.playerCurrentRoom.getData().getName(), oldRoom.getData().getName())) {
            changeRoomEvent(oldRoom, RoomUtils.playerCurrentRoom);
            oldRoom = RoomUtils.playerCurrentRoom;
        }
    }

    public void changeRoomEvent(Room old, Room room) {
        Room mainroom = RoomUtils.getMainRoom(room);
        new DungeonEvent.ChangeRoom(old, room, mainroom).post();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Unload event) {
        reset();
    }

//    @SubscribeEvent
//    public void onPacket(PacketEvent.Receive event) {
//        if(mc == null || mc.theWorld == null || mc.thePlayer == null) return;
//        Packet<?> packet = event.packet;
//        if (packet instanceof S34PacketMaps && Loc.area.is(Island.Dungeon) && DungeonInfo.dungeonMap == null) {
//            if (mc.thePlayer.getHeldItem() != null) {
//                if (mc.thePlayer.getHeldItem().getItem() == Items.bow) return;
//            }
//            int id = ((S34PacketMaps) packet).getMapId();
//            if ((id & 1000) == 0) {
//                if (DungeonInfo.guessMapData == null) return;
//                MapData guess = (MapData) mc.theWorld.getMapStorage().loadData(MapData.class, "map_" + id);
//                if (guess != null && DungeonInfo.guessMapData.mapDecorations.values().stream().anyMatch(d -> d.func_176110_a() == 1)) {
//                    DungeonInfo.guessMapData = guess;
//                    DungeonMapColorParser.updateMap(guess);
//                }
//            }
//        }
//    }

    @SubscribeEvent
    public void onRoomChange(DungeonEvent.ChangeRoom event) {
        if (event.mainRoom == null) {
            Room main = RoomUtils.getMainRoom(event.room);
            if (main == null) {
                TaskComponent.onTick(0, () -> onRoomChange(event, 0));
                return;
            }
            RoomUtils.currentMainRoom = main;
        } else {
            RoomUtils.currentMainRoom = event.mainRoom;
        }
    }

    public void onRoomChange(DungeonEvent.ChangeRoom event, int tries) {
        if (event.mainRoom == null) {
            Room main = RoomUtils.getMainRoom(event.room);
            if (main == null) {
                if (tries > 5) return;
                TaskComponent.onTick(0, () -> onRoomChange(event, tries + 1));
                return;
            }
            RoomUtils.currentMainRoom = main;
        } else {
            RoomUtils.currentMainRoom = event.mainRoom;
        }
    }

    @SubscribeEvent
    public void bossEntered(DungeonEvent.EnterBoss event) {
        RoomUtils.playerCurrentRoom = null;
        RoomUtils.currentMainRoom = null;
    }
}
