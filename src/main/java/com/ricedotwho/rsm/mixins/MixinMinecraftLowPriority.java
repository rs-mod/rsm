package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.PacketOrderManager;
import com.ricedotwho.rsm.component.impl.SwapManager;
import com.ricedotwho.rsm.event.impl.client.AttackPacketEvent;
import com.ricedotwho.rsm.event.impl.client.UsePacketEvent;
import com.ricedotwho.rsm.event.impl.game.RawTickEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 468) // Low prio for SwapManager
public abstract class MixinMinecraftLowPriority {
    @Shadow
    private @Nullable Overlay overlay;

    @Unique
    private boolean bla = false;
    @Unique
    private boolean blu = false;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickStart(CallbackInfo ci) {
        SwapManager.onPreTickStart(); // Must be called first, unless you have a good reason don't change the order
        PacketOrderManager.onPreTickStart();

        //boolean c = TickFreeze.isFrozen();
        RawTickEvent event = new RawTickEvent();
        boolean bl = event.post();
        if (bl) {
            ci.cancel();
            return;
        }
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    public void onHandleKeybinds(CallbackInfo ci) {
        bla = true;
        blu = true;
    }

    @Shadow
    protected abstract void handleKeybinds();

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"))
    public void onHandleKeyBinds(Minecraft instance) {
        // Need to cancel it
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showDebugScreen()Z"))
    public void onGetShowDebugScreen(CallbackInfo ci) { // Right before onHandleKeyBinds
        if (this.overlay == null && Minecraft.getInstance().player != null) { // && (screen == null || (!(this.screen instanceof AbstractContainerScreen<?>)))
            Profiler.get().popPush("Keybindings");
            // Needed to still call the packet order
            this.handleKeybinds();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 13), method = "handleKeybinds")
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 14), method = "handleKeybinds")
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 16), method = "handleKeybinds")
    public void onHandleInputEvent3(CallbackInfo ci) {
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 17), method = "handleKeybinds")
    public void onHandleInputEvent4(CallbackInfo ci) {
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

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    private void adjustClientSlot(Inventory instance, int i) {
        SwapManager.swapClientSlot(i, instance);
    }
}
