package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.HidePlayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onRender(Entity entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (HidePlayers.shouldHide(entity)) cir.setReturnValue(false);
    }
}
