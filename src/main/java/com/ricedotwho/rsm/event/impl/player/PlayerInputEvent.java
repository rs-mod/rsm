package com.ricedotwho.rsm.event.impl.player;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;

public class PlayerInputEvent extends Event {

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Attack extends PlayerInputEvent {
        private final HitResult result;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Use extends PlayerInputEvent {
        private final InteractionHand hand;
        private final HitResult result;
        private final float yRot;
        private final float xRot;
    }
}
