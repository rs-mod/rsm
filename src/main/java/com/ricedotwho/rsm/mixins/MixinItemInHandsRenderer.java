package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandsRenderer {

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot(F)F"))
    private float spoofPitch(LocalPlayer instance, float f) {
        return Mth.lerp(f, CameraHandler.lastPitch, CameraHandler.getPitch(instance.getXRot()));
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    private float spoofedViewPitch(LocalPlayer instance, float f) {
        //for some reason in getViewRot the subtick doesnt matter
        return CameraHandler.getPitch(instance.getViewXRot(f));
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
    private float spoofedViewYaw(LocalPlayer instance, float f) {
        //for some reason in getViewRot the subtick doesnt matter
        return CameraHandler.getYaw(instance.getViewYRot(f));
    }
}
