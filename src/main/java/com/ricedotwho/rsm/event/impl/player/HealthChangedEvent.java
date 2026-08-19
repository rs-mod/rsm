package com.ricedotwho.rsm.event.impl.player;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

public sealed abstract class HealthChangedEvent extends Event {
    @Getter
    public final static class Heal extends HealthChangedEvent {
        private final float totalHealth;
        private final float percentage;
        private final float amount;
        private final float healthBefore;
        private final float healthAfter;

        public Heal(float totalHealth, float percentage, float healthBefore, float healthAfter) {
            this.totalHealth = totalHealth;
            this.percentage = percentage;
            this.amount = healthAfter - healthBefore;
            this.healthBefore = healthBefore;
            this.healthAfter = healthAfter;
        }
    }

    @Getter
    public final static class Hurt extends HealthChangedEvent {
        private final float totalHealth;
        private final float percentage;
        private final float amount;
        private final float healthBefore;
        private final float healthAfter;

        public Hurt(float totalHealth, float percentage, float healthBefore, float healthAfter) {
            this.totalHealth = totalHealth;
            this.percentage = percentage;
            this.amount = healthBefore - healthAfter;
            this.healthBefore = healthBefore;
            this.healthAfter = healthAfter;
        }
    }
}