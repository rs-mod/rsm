package com.ricedotwho.rsm.component.impl.map;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.*;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.MapUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.item.Items;

import java.util.Objects;

public class Map extends ModComponent {
    private Room oldRoom = null;
    @Getter
    private static Room currentRoom = null;

    public Map() {
        super("Map");
    }

    public static void reset() {
        DungeonInfo.reset();
        MapUtils.calibrated = false;
        DungeonScanner.hasScanned = false;
    }

    @SubscribeEvent
    public void updateMap(ClientTickEvent.Start event) {
        if (Dungeon.isInBoss() || !Location.getArea().is(Island.Dungeon) || mc.player == null) return;

        if (!MapUtils.calibrated) {
            if (DungeonInfo.getDungeonMap() == null) {
                DungeonInfo.setDungeonMap(MapUtils.getMapData());
            }

            MapUtils.calibrated = MapUtils.calibrateMap();
        } else {
            if (DungeonInfo.getDungeonMap() != null) MapUpdater.updateRooms(DungeonInfo.getDungeonMap());
            DungeonInfo.getUniqueRooms().forEach(UniqueRoom::update);
        }

        if (DungeonScanner.shouldScan()) {
            DungeonScanner.scan();
        }

        updateCurrentRoom();
        if (currentRoom == null) return;
        if (oldRoom == null) {
            oldRoom = currentRoom;
        }


        if (!Objects.equals(currentRoom.getData().name(), oldRoom.getData().name())) {
            UniqueRoom uni = currentRoom.getUniqueRoom();
            new DungeonEvent.ChangeRoom(oldRoom, currentRoom, uni).post();
            oldRoom = currentRoom;
        }
    }

    private void updateCurrentRoom() {
        currentRoom = ScanUtils.getRoomFromPos((int) mc.player.position().x(), (int) mc.player.position().z());
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        if(mc.level == null || mc.player == null) return;
        if (event.getPacket() instanceof ClientboundMapItemDataPacket packet && Location.getArea().is(Island.Dungeon) && DungeonInfo.getDungeonMap() == null) {
            if (mc.player.getInventory().getSelectedItem().getItem() == Items.BOW) return;
            int id = packet.mapId().id();
            if ((id & 1000) == 0) {
                if (DungeonInfo.getGuessMapData() == null) return;
                packet.applyToMap(DungeonInfo.getGuessMapData());
                DungeonMapColorParser.updateMap(DungeonInfo.getGuessMapData());
            }
        }
    }

    @SubscribeEvent
    public void bossEntered(DungeonEvent.EnterBoss event) {
        currentRoom = null;
        oldRoom = null;
    }
}
