package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MouseInputEvent extends Event {

    @Getter
    @AllArgsConstructor
    public static class Click extends MouseInputEvent {
        private boolean down;
        private final int button;
        private final int modifiers;
    }

    @Getter
    @AllArgsConstructor
    public static class Scroll extends MouseInputEvent {
        private final double direction;
    }

    @Getter
    @AllArgsConstructor
    public static class Move extends MouseInputEvent {
        private final double posX;
        private final double posY;
    }
}
