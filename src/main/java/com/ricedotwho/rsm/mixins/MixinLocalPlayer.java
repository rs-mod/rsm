package com.ricedotwho.rsm.mixins;

import com.mojang.authlib.GameProfile;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    public MixinLocalPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    @Inject(method = "getViewXRot", at = @At("RETURN"), cancellable = true)
    public void getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (ClickGUI.getInterpolateCamera().getValue())
            cir.setReturnValue(super.getXRot(tickDelta));
    }

    @Inject(method = "getViewYRot", at = @At("RETURN"), cancellable = true)
    public void getYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (ClickGUI.getInterpolateCamera().getValue())
            cir.setReturnValue(super.getYRot(tickDelta));
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        if (new ClientTickEvent.Player((LocalPlayer) (Object) this).post()) {
            ci.cancel();
        }
    }

    @Redirect(method = "applyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float returnCamPitch(LocalPlayer instance) {
        return CameraHandler.getPitch(instance.getXRot());
    }

    @Redirect(method = "applyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float returnCamYaw(LocalPlayer instance) {
        return CameraHandler.getYaw(instance.getYRot());
    }
}