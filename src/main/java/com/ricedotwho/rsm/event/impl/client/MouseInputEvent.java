package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.SmoothDouble;

public class MouseInputEvent extends Event {

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Click extends MouseInputEvent {
        private boolean down;
        private final int button;
        private final int modifiers;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Scroll extends MouseInputEvent {
        private final double direction;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Move extends MouseInputEvent {
        private final double posX;
        private final double posY;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class TurnPlayer extends MouseInputEvent {
        private final double d;
        private final double dx;
        private final double dy;
        private final SmoothDouble smoothTurnX;
        private final SmoothDouble smoothTurnY;
    }
}
