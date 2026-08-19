package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;

public abstract sealed class TickEvent extends Event {
    @Getter
    @AllArgsConstructor
    @Cancellable
    public final static class Start extends TickEvent { }

    @Getter
    @AllArgsConstructor
    public final static class End extends TickEvent { }

    @Getter
    @AllArgsConstructor
    public final static class ClientStart extends TickEvent {
        private final long time;
    }

    @Getter
    @AllArgsConstructor
    public final static class ClientEnd extends TickEvent {
        private final long time;
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
