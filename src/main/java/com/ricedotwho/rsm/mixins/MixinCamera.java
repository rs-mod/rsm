package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.event.impl.render.CameraSetupEvent;
import com.ricedotwho.rsm.module.impl.render.CrouchAnimation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class)
public abstract class MixinCamera {

//    @Inject(method = "position", at = @At("HEAD"), cancellable = true)
//    private void onGetPosition(CallbackInfoReturnable<Vec3> cir) {
//        CameraHandler.onGetCameraPos(cir);
//    }
//
//    @Inject(method = "blockPosition", at = @At("HEAD"), cancellable = true)
//    private void onGetBlockPos(CallbackInfoReturnable<BlockPos> cir) {
//        CameraHandler.onGetCameraBlockPos(cir);
//    }
//
//
//    @Inject(method = "xRot", at = @At("HEAD"), cancellable = true)
//    private void onGetPitch(CallbackInfoReturnable<Float> cir) {
//        CameraHandler.onGetCameraPitch(cir);
//    }
//
//    @Inject(method = "yRot", at = @At("HEAD"), cancellable = true)
//    private void onGetYaw(CallbackInfoReturnable<Float> cir) {
//        CameraHandler.onGetCameraYaw(cir);
//    }
//
//    @Inject(method = "rotation", at = @At("HEAD"), cancellable = true)
//    private void onGetRotation(CallbackInfoReturnable<Quaternionf> cir) {
//        CameraHandler.onGetCameraRotation(cir);
//    }

    @Shadow
    protected abstract void setRotation(float f, float g);

    @Shadow
    protected abstract void setPosition(Vec3 vec3);

    @Shadow
    private Vec3 position;

    @Shadow
    public abstract float yaw();

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.5F))
    public float modifyCrouchSpeed(float original) {
        Float factor = CrouchAnimation.getFactor();
        return factor == null ? original : factor;
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void postStart(DeltaTracker deltaTracker, CallbackInfo ci) {
        new CameraSetupEvent().post();
    }

    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", shift = At.Shift.AFTER, ordinal = 0)) //inject after incase other mod redirects
    private void spoofRotation(float partialTicks, CallbackInfo ci) {
        this.setRotation(CameraHandler.getYaw(this.yRot), CameraHandler.getPitch(this.xRot));
    }
    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", shift = At.Shift.AFTER, ordinal = 1)) //inject after incase other mod redirects
    private void spoofRotation1(float partialTicks, CallbackInfo ci) {
        this.setRotation(CameraHandler.getYaw(this.yRot), CameraHandler.getPitch(this.xRot));
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void spoofPosition(float partialTicks, CallbackInfo ci) {
        this.setPosition(CameraHandler.getPos(new Vec3(this.position.x, this.position.y, this.position.z), partialTicks, this.eyeHeightOld, this.eyeHeight));
    }

//    @Inject(method = "extractRenderState", at = @At("TAIL"))
//    private void setCameraPos(Level level, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
//        this.setPosition(CameraHandler.getPos(new Vec3(this.position.x, this.position.y, this.position.z)));
//    }
}
