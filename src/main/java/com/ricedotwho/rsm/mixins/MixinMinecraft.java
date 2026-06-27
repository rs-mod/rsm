package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.module.impl.player.ChestHitFix;
import com.ricedotwho.rsm.module.impl.player.WorldBorderFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 650)
public abstract class MixinMinecraft {

    @Shadow
    public HitResult hitResult;

    @Shadow
    public LocalPlayer player;

    @Shadow
    public MultiPlayerGameMode gameMode;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void onAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!player.isHandsBusy() && new PlayerInputEvent.Attack(hitResult).post()) cir.setReturnValue(true);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    public void onUseItem(CallbackInfo ci) {
        if (player != null && !gameMode.isDestroying() && !player.isHandsBusy() && new PlayerInputEvent.Use(hitResult, player.getYRot(), player.getXRot()).post()) ci.cancel();
    }

    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    private boolean doChestHitFix(MultiPlayerGameMode instance, Operation<Boolean> original) {
        if (ChestHitFix.shouldRun()) {
            return false;
        }
        return original.call(instance);
    }

    /// For right click
    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFix(WorldBorder instance, BlockPos blockPos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, blockPos);
    }
}
