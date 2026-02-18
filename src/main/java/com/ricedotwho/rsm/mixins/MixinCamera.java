package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Camera.class)
public abstract class MixinCamera {

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private void onGetPosition(CallbackInfoReturnable<Vec3> cir) {
        CameraHandler.onGetCameraPos(cir);
    }

    @Inject(method = "getXRot", at = @At("HEAD"), cancellable = true)
    private void onGetPitch(CallbackInfoReturnable<Float> cir) {
        CameraHandler.onGetCameraPitch(cir);
    }

    @Inject(method = "getYRot", at = @At("HEAD"), cancellable = true)
    private void onGetYaw(CallbackInfoReturnable<Float> cir) {
        CameraHandler.onGetCameraYaw(cir);
    }

    @Inject(method = "rotation", at = @At("HEAD"), cancellable = true)
    private void onGetRotation(CallbackInfoReturnable<Quaternionf> cir) {
        CameraHandler.onGetCameraRotation(cir);
    }
}
