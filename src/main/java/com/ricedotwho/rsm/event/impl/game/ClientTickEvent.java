package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;

public class ClientTickEvent extends Event {

    @Getter
    @AllArgsConstructor
    public static class Start extends ClientTickEvent {
        private final long time;
    }

    @Getter
    @AllArgsConstructor
    public static class End extends ClientTickEvent {
        private final long time;
    }

    @Getter
    @Cancellable
    @AllArgsConstructor
    public static class Player extends ClientTickEvent {
        private final LocalPlayer player;
    }
}
