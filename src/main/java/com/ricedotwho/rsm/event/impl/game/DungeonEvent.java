package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import com.ricedotwho.rsm.location.Floor;
import com.ricedotwho.rsm.managers.dungeon.map.map.Room;
import com.ricedotwho.rsm.managers.dungeon.map.map.RoomState;
import com.ricedotwho.rsm.managers.dungeon.map.map.UniqueRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;

public sealed abstract class DungeonEvent extends Event {
    @Getter
    public final static class Joined extends DungeonEvent {
        private final Floor floor;
        public Joined(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    @Cancellable
    public final static class Start extends DungeonEvent {
        private final Floor floor;
        public Start(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    @Cancellable
    public final static class End extends DungeonEvent {
        private final Floor floor;
        public End(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    public final static class EnterBoss extends DungeonEvent {
        private final Floor floor;
        public EnterBoss(Floor floor) {
            this.floor = floor;
        }
    }

    @Getter
    public final static class ChangeRoom extends DungeonEvent {
        private final Room oldRoom;
        private final UniqueRoom unique;
        private final Room room;

        /**
         *
         * @param oldRoom will be null when entering the first room (Entrance)
         * @param room current room
         * @param unique unique room
         */
        public ChangeRoom(Room oldRoom, Room room, UniqueRoom unique) {
            this.oldRoom = oldRoom;
            this.room = room;
            this.unique = unique;
        }
    }

    @Getter
    public final static class RoomScanned extends DungeonEvent {
        private final UniqueRoom unique;
        public RoomScanned(UniqueRoom unique) {
            this.unique = unique;
        }
    }

    @Getter
    public final static class RoomLoad extends DungeonEvent {
        private final Room room;
        public RoomLoad(Room room) {
            this.room = room;
        }
    }

    public final static class ScanComplete extends DungeonEvent {

    }

    public final static class BloodOpened extends DungeonEvent {
        public BloodOpened() {}
    }

    @Getter
    @AllArgsConstructor
    public final static class StateChange extends DungeonEvent {
        private final Room room;
        private final RoomState oldState;
        private final RoomState newState;
    }
}
