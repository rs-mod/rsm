package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.event.Cancellable;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;

public class DungeonEvent extends Event {
    @Getter
    public static class Joined extends DungeonEvent {
        private final Floor floor;
        public Joined(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    @Cancellable
    public static class Start extends DungeonEvent {
        private final Floor floor;
        public Start(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    @Cancellable
    public static class End extends DungeonEvent {
        public Packet<?> packet;
        public final Floor floor;
        public End(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    public static class EnterBoss extends DungeonEvent {
        public final Floor floor;
        public EnterBoss(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    public static class ChangeRoom extends DungeonEvent {
        public final Room oldRoom;
        public final UniqueRoom unique;
        public final Room room;
        public ChangeRoom(Room oldRoom, Room room, UniqueRoom unique) {
            this.oldRoom = oldRoom;
            this.room = room;
            this.unique = unique;
        }
    }

    @Getter
    public static class RoomScanned extends DungeonEvent {
        private final UniqueRoom unique;
        public RoomScanned(UniqueRoom unique) {
            this.unique = unique;
        }
    }

    public static class BloodOpened extends Event {
        public BloodOpened() {}
    }
}
