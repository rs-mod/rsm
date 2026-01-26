package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    // tbh idk if this will die or not
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        new Render3DEvent(deltaTracker).post();
    }
}
