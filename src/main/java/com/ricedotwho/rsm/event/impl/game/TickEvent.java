package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import com.ricedotwho.rsm.managers.EventDispatcher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;

public abstract sealed class TickEvent extends Event {
    @Getter
    @Cancellable
    public final static class Start extends TickEvent {
        private final long time;

        public Start() {
            this.time = EventDispatcher.onTickStart();
        }
    }

    @Getter
    public final static class End extends TickEvent {
        private final long time;

        public End() {
            this.time = EventDispatcher.getClientLifeTime();
        }
    }

    @Getter
    public final static class ClientStart extends TickEvent {
        private final long time;

        public ClientStart() {
            this.time = EventDispatcher.getClientLifeTime();
        }
    }

    @Getter
    public final static class ClientEnd extends TickEvent {
        private final long time;

        public ClientEnd() {
            this.time = EventDispatcher.getClientLifeTime();
        }
    }

    @Getter
    @Cancellable
    @AllArgsConstructor
    public final static class Player extends TickEvent {
        private final LocalPlayer player;
    }

    @Getter
    @AllArgsConstructor
    public final static class Server extends TickEvent {
        private final int id;
        private final long time;
    }
}
