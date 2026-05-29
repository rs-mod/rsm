package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.game.RawTickEvent;

public class Sneak extends ModComponent {
    public Sneak() {
        super("Sneak");
    }

    private static int ticksLeft = 0;

    public static void sneak(int ticks) {
        ticksLeft = Math.max(ticks, ticksLeft);
    }

    @SubscribeEvent
    public void onTick(RawTickEvent tick) {
        ticksLeft--; //idc that this will underflow after a few years of runtime
    }

    @SubscribeEvent
    public void onKey(InputPollEvent event) {
        if (ticksLeft > 0) {
            event.getInput().shift(true);
        }
    }

    public void stopSneak() {
        ticksLeft = 0; // IDK maybe
    }
}
