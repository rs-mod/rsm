package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.IMixin.IMultiPlayerGameMode;
import com.ricedotwho.rsm.component.impl.SwapManager;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.impl.player.WorldBorderFix;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements IMultiPlayerGameMode {

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Shadow
    private int carriedIndex;

    @Override
    public void sendPacketSequenced(ClientLevel world, PredictiveAction packetCreator) {
        this.startPrediction(world, packetCreator);
    }

    @Override
    public void syncSlot() {
        this.ensureHasSentCarriedItem();
    }

    @Inject(method = "ensureHasSentCarriedItem", at = @At("HEAD"), cancellable = true)
    public void onSyncSlot(CallbackInfo ci) {
        if (!SwapManager.onEnsureHasSentCarriedItem(this.carriedIndex)) ci.cancel();
    }

//    @Redirect(method = "interactAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;createInteractionPacket(Lnet/minecraft/world/entity/Entity;ZLnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;"))
//    public ServerboundInteractPacket onCreateInteractionWhatver(Entity entity, boolean bl, InteractionHand interactionHand, Vec3 vec3) {
//        ChatUtils.chat(entity.position() + " : " + vec3);
//        return ServerboundInteractPacket.createInteractionPacket(entity, bl, interactionHand, vec3);
//    }

    /// For right click
    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFixUse(WorldBorder instance, BlockPos blockPos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, blockPos);
    }

    /// For left click
    @WrapOperation(method = "startDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFixStartDestroy(WorldBorder instance, BlockPos blockPos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, blockPos);
    }
    @WrapOperation(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean doWorldBorderFixContinueDestroy(WorldBorder instance, BlockPos blockPos, Operation<Boolean> original) {
        if (Location.isInSkyblock() && WorldBorderFix.getEnabled()) {
            return true;
        }
        return original.call(instance, blockPos);
    }

    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
    void handleInventoryMouseClick(int containerID, int slotID, int button, ClickType clickType, Player player, CallbackInfo ci) {
        new GuiEvent.HandleClick(containerID, slotID, button, clickType).post();
    }
}
