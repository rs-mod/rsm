package com.ricedotwho.rsm.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.impl.client.InputEvent;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class MixinKeyBinding {

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressed(InputConstants.Key key, CallbackInfo ci) {
        if (new InputEvent(key).post()) ci.cancel();
    }
}
