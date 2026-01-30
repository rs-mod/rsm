package com.ricedotwho.rsm.event.impl.player;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

public class HealthChangedEvent extends Event {
    @Getter
    public static class Heal extends HealthChangedEvent {
        private final float amount;
        private final float healthBefore;
        private final float healthAfter;

        public Heal(float healthBefore, float healthAfter) {
            this.amount = healthAfter - healthBefore;
            this.healthBefore = healthBefore;
            this.healthAfter = healthAfter;
        }
    }

    @Getter
    public static class Hurt extends HealthChangedEvent {
        private final float amount;
        private final float healthBefore;
        private final float healthAfter;

        public Hurt(float healthBefore, float healthAfter) {
            this.amount = healthBefore - healthAfter;
            this.healthBefore = healthBefore;
            this.healthAfter = healthAfter;
        }
    }
}