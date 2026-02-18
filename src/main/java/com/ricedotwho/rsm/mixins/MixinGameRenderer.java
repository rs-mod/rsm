package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.Freecam;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", ordinal = 0))
    private boolean onRenderLevel(CameraType instance) {
        return instance.isFirstPerson() && !Freecam.isDetached();
    }
}
