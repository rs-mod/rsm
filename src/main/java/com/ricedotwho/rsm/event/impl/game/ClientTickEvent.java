package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
