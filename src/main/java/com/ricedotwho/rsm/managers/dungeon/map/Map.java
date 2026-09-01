package com.ricedotwho.rsm.managers.dungeon.map;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.map.handler.*;
import com.ricedotwho.rsm.managers.dungeon.map.map.Room;
import com.ricedotwho.rsm.managers.dungeon.map.map.UniqueRoom;
import com.ricedotwho.rsm.managers.dungeon.map.utils.MapUtils;
import com.ricedotwho.rsm.managers.dungeon.map.utils.ScanUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Register
@UtilityClass
public class Map {
    private Room oldRoom = null;
    @Getter
    private Room currentRoom = null;

    public void reset() {
        DungeonInfo.reset();
        MapUtils.calibrated = false;
        DungeonScanner.hasScanned = false;
        oldRoom = null;
        currentRoom = null;
    }

    @SubscribeEvent
    private void updateMap(TickEvent.ClientStart event) {
        if (Dungeon.isInBoss() || !Location.getArea().is(Island.Dungeon) || !Location.getFloor().isDungeons() || mc.player == null) return;
        ProfilerFiller profiler = Profiler.get();

        if (DungeonScanner.shouldScan() && event.getTime() % 5 == 0) {
            profiler.push("Scan");
            DungeonScanner.scan();

            profiler.popPush("UniqueRoom Update");
            DungeonInfo.getUniqueRooms().forEach(UniqueRoom::update);
            profiler.pop();
        }

        profiler.push("Update Current");
        updateCurrentRoom();
        profiler.pop();
        if (currentRoom == null || currentRoom.getUniqueRoom() == null) return;

        boolean fireUnique = oldRoom == null || oldRoom.getUniqueRoom() == null || !oldRoom.getUniqueRoom().getName().equals(currentRoom.getUniqueRoom().getName());

        if (oldRoom == null || oldRoom.getData() != currentRoom.getData()) {
            UniqueRoom uni = currentRoom.getUniqueRoom();
            new DungeonEvent.ChangeRoom(oldRoom, currentRoom, uni).post();
            if (fireUnique) new DungeonEvent.ChangeUnique(oldRoom, currentRoom, uni).post();
            oldRoom = currentRoom;
        }
    }

    private void updateCurrentRoom() {
        assert mc.player != null;
        currentRoom = ScanUtils.getRoomFromPos((int) mc.player.position().x(), (int) mc.player.position().z());
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    private void onPacket(PacketEvent.MainReceivePost event, ClientboundMapItemDataPacket packet) {
        if (mc.level == null || mc.player == null || !Location.getArea().is(Island.Dungeon)) return;
        ItemStack map =  mc.player.getInventory().getSelectedItem();
        if (map.getItem() != Items.FILLED_MAP || packet.mapId() != map.get(DataComponents.MAP_ID)) return;

        if (!MapUtils.calibrated) {
            if (DungeonInfo.getDungeonMap() == null) {
                DungeonInfo.setDungeonMap(MapUtils.getMapData());
            }

            MapUtils.calibrated = MapUtils.calibrateMap();
        } else if (DungeonInfo.getDungeonMap() != null) {
            MapUpdater.updateRooms(DungeonInfo.getDungeonMap());
        }
    }

    @SubscribeEvent
    private void bossEntered(DungeonEvent.EnterBoss event) {
        currentRoom = null;
        oldRoom = null;
    }
}
