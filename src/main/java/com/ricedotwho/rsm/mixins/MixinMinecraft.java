package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.module.impl.player.ChestHitFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public HitResult hitResult;

    @Shadow
    public LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void onAttack(CallbackInfoReturnable<Boolean> cir) {
        if (new PlayerInputEvent.Attack(hitResult).post()) cir.setReturnValue(true);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    public void onUseItem(CallbackInfo ci) {
        if (player != null && new PlayerInputEvent.Use(hitResult, player.getYRot(), player.getXRot()).post()) ci.cancel();
    }


    @Redirect(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    private boolean doChestHitFix(MultiPlayerGameMode instance) {
        if (ChestHitFix.shouldRun()) {
            return false;
        }
        return instance.isDestroying();
    }
}
