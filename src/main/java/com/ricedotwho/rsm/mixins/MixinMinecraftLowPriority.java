package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.component.impl.PacketOrderManager;
import com.ricedotwho.rsm.component.impl.SwapManager;
import com.ricedotwho.rsm.event.impl.client.AttackPacketEvent;
import com.ricedotwho.rsm.event.impl.client.UsePacketEvent;
import com.ricedotwho.rsm.event.impl.game.RawTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 468) // Low prio for SwapManager
public abstract class MixinMinecraftLowPriority {

    @Unique
    private boolean bla = false;
    @Unique
    private boolean blu = false;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickStart(CallbackInfo ci) {
        SwapManager.onPreTickStart(); // Must be called first, unless you have a good reason don't change the order
        PacketOrderManager.onPreTickStart();

        //boolean c = TickFreeze.isFrozen();
        if (new RawTickEvent().post()) {
            ci.cancel();
            return;
        }
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    public void onHandleKeybinds(CallbackInfo ci) {
        bla = true;
        blu = true;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 14), method = "handleKeybinds")
    public void onHandleInputEvent(CallbackInfo ci) {
        if (bla) {
            if (new AttackPacketEvent.Pre().post()){
                bla = true;
                return;
            }
            PacketOrderManager.execute(PacketOrderManager.STATE.ATTACK);
            new AttackPacketEvent.Post().post();
            bla = false;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 15), method = "handleKeybinds")
    public void onHandleInputEvent2(CallbackInfo ci) {
        if (blu) {
            if (new UsePacketEvent.Pre().post()){
                blu = true;
                return;
            }
            PacketOrderManager.execute(PacketOrderManager.STATE.ITEM_USE);
            new UsePacketEvent.Post().post();
            blu = false;
            // Need bl because called in whileLoop
        }
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    private void adjustClientSlot(Inventory inventory, int slot, Operation<Void> original) {
        SwapManager.swapClientSlot(slot, inventory);
    }
}
