package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.event.impl.render.CameraSetupEvent;
import com.ricedotwho.rsm.module.impl.render.CrouchAnimation;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
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

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.5F))
    public float modifyCrouchSpeed(float original) {
        Float factor = CrouchAnimation.getFactor();
        return factor == null ? original : factor;
    }

    @Inject(method = "setup", at = @At("HEAD"))
    private void postStart(Level level, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
        new CameraSetupEvent().post();
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void setCameraYawPitch(Camera camera, float yaw, float pitch) {
        this.setRotation(CameraHandler.getYaw(yaw), CameraHandler.getPitch(pitch));
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void setCameraPos(Level level, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
        this.setPosition(CameraHandler.getPos(new Vec3(this.position.x, this.position.y, this.position.z)));
    }
}
