package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandsRenderer {

    @ModifyExpressionValue(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    public float getPitch(float original) {
        return CameraHandler.getPitch(original);
    }

//    @ModifyExpressionValue(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
//    public float getYaw(float original) {
//        return CameraHandler.getYaw(original);
//    }
}
