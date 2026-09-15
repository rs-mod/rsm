package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.module.impl.player.NoBreakReset;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandsRenderer {

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot(F)F"))
    private float spoofPitch(LocalPlayer instance, float f) {
        return Mth.lerp(f, CameraHandler.lastPitch, CameraHandler.getPitch(instance.getXRot()));
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    private float spoofedViewPitch(LocalPlayer instance, float a) {
        //for some reason in getViewRot the subtick doesn't matter
        return CameraHandler.getPitch(instance.getViewXRot(a));
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
    private float spoofedViewYaw(LocalPlayer instance, float a) {
        //for some reason in getViewRot the subtick doesn't matter
        return CameraHandler.getYaw(instance.getViewYRot(a));
    }

    @Inject(at = @At("RETURN"), method = "shouldInstantlyReplaceVisibleItem", cancellable = true)
    private void onShouldInstantlyReplaceVisibleItem(ItemStack currentlyVisibleItem, ItemStack expectedItem, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && NoBreakReset.getInstance().isEnabled() && NoBreakReset.getInstance().getStopFlicker().getValue()) {
            cir.setReturnValue(NoBreakReset.compare(currentlyVisibleItem, expectedItem));
        }
    }
}
