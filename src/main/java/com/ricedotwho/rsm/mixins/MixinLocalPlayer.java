package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.managers.NoRotateManager;
import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.module.impl.movement.AutoSprint;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    public MixinLocalPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    @Inject(method = "getViewXRot", at = @At("RETURN"), cancellable = true)
    public void getPitch(float a, CallbackInfoReturnable<Float> cir) {
        if (UniversalSettings.getInterpolateCamera().getValue() && NoRotateManager.isLerp())
            cir.setReturnValue(super.getXRot(a));
    }

    @Inject(method = "getViewYRot", at = @At("RETURN"), cancellable = true)
    public void getYaw(float a, CallbackInfoReturnable<Float> cir) {
        if (UniversalSettings.getInterpolateCamera().getValue() && NoRotateManager.isLerp())
            cir.setReturnValue(super.getYRot(a));
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        if (new TickEvent.Player((LocalPlayer) (Object) this).post()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "applyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float spoofPitch(float original) {
        return CameraHandler.getPitch(original);
    }

    @ModifyExpressionValue(method = "applyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float spoofYaw(float original) {
        return CameraHandler.getYaw(original);
    }

    // Modify the position used for pick
    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 0)
    private static Vec3 pickPosition(Vec3 from) {
        return CameraHandler.onGetPositionForHit(from);
    }

    // Modify the rotation used for pick
    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 1)
    private static Vec3 pickRotation(Vec3 direction) {
        return CameraHandler.onGetRotationForHit(direction);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Input;sprint()Z"))
    private boolean rsm$autoSprint(boolean original) {
        return original || AutoSprint.getInstance().isEnabled();
    }
}