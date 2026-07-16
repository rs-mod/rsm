package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.module.impl.dungeon.DungeonBreaker;
import com.ricedotwho.rsm.module.impl.player.ChestHitFix;
import com.ricedotwho.rsm.module.impl.player.WorldBorderFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void startAttackMidLoop(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand hand1) {
        if (new PlayerInputEvent.Use(hand1, hitResult, player.getYRot(), player.getXRot()).post()) ci.cancel();
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

    @ModifyArg(method = "handleKeybinds()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
    public boolean handleInputEventsContinueAttack(boolean bl) {
        if (DungeonBreaker.shouldContinueAttack(bl)) {
            return false;
        }
        return bl;
    }
}
