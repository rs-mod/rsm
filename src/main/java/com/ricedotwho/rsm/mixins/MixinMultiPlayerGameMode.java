package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.impl.player.NoBreakReset;
import com.ricedotwho.rsm.module.impl.player.WorldBorderFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Shadow
    private ItemStack destroyingItem;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private BlockPos destroyBlockPos;

    /// For right click
    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFixUse(WorldBorder instance, BlockPos pos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, pos);
    }

    /// For left click
    @WrapOperation(method = "startDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFixStartDestroy(WorldBorder instance, BlockPos pos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, pos);
    }
    @WrapOperation(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFixContinueDestroy(WorldBorder instance, BlockPos pos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, pos);
    }

    @Inject(method = "handleContainerInput", at = @At("HEAD"))
    void handleInventoryMouseClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        new GuiEvent.HandleClick(containerId, slotNum, buttonNum, containerInput).post();
    }

    @Inject(at = @At("RETURN"), method = "sameDestroyTarget", cancellable = true)
    private void itemStackCompare(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || !pos.equals(destroyBlockPos) || !NoBreakReset.getInstance().isEnabled()) return;

        assert minecraft.player != null;
        ItemStack hand = minecraft.player.getMainHandItem();
        if (NoBreakReset.compare(hand, destroyingItem)) {
            destroyingItem = hand;
            cir.setReturnValue(true);
        }
    }
}
