package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.HidePlayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private <T extends Entity> void hideNametags(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (HidePlayers.shouldHideNametag(entity)) {
            cir.setReturnValue(false);
        }
    }
}
