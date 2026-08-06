package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.player.Input;

import static com.ricedotwho.rsm.utils.Accessor.mc;

@Register
@UtilityClass
public class Sneak {

    private int ticksLeft = 0;
    private boolean heldSneakSinceStart = false;

    public void sneak(int ticks) {
        ticksLeft = Math.max(ticks, ticksLeft);
        heldSneakSinceStart = mc.options.keyShift.isDown();
    }

    public void stopSneak() {
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
