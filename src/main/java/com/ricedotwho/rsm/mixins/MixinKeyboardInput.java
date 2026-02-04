package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/KeyboardInput;calculateImpulse(ZZ)F", ordinal = 0))
    private void onTick(CallbackInfo ci) {
        new InputPollEvent(((KeyboardInput) (Object) this).keyPresses, input -> ((KeyboardInput) (Object) this).keyPresses = input).post(); // Will edit the keypresses
    }
}
