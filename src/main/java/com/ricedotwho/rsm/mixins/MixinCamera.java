package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ricedotwho.rsm.event.impl.render.CameraSetupEvent;
import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.module.impl.player.CrouchSpeed;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class)
public abstract class MixinCamera {

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    private Vec3 position;

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @Shadow
    private Entity entity;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeProbe;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE))
    public void modifyCrouchSpeed(CallbackInfo ci) {
        Float factor = CrouchSpeed.getFactor();
        if (factor == null) return;
        this.eyeHeight = this.eyeHeightOld + ((this.entity.getEyeHeight() - this.eyeHeightOld) * factor);
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

    @ModifyExpressionValue(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isMirrored()Z"))
    private boolean noMirrorIfCustomPos(boolean original) {
        return original && !CameraHandler.hasPosition();
    }

}
