package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import net.minecraft.world.entity.player.Input;

public class Sneak extends ModComponent {
    public Sneak() {
        super("Sneak");
    }

    private static int ticksLeft = 0;
    private static boolean heldSneakSinceStart = false;

    public static void sneak(int ticks) {
        ticksLeft = Math.max(ticks, ticksLeft);
        heldSneakSinceStart = mc.options.keyShift.isDown();
    }

    public static void stopSneak() {
        ticksLeft = 0;
    }

    @SubscribeEvent
    private void onKeyInput(InputPollEvent event) {
        if (ticksLeft-- <= 0) return;
        Input oldInputs = event.getClientInput();

        if (oldInputs.shift()) {
            if (!heldSneakSinceStart) {
                ticksLeft = 0;
                return;
            }
        } else {
            heldSneakSinceStart = false;
        }

        Input newInputs = new Input(oldInputs.forward(), oldInputs.backward(), oldInputs.left(), oldInputs.right(), oldInputs.jump(), true, oldInputs.sprint());
        event.getInput().apply(newInputs);
    }
}
