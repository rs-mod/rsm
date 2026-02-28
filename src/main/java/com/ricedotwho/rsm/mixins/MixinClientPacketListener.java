package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.game.TerminalEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerChatEvent;
import com.ricedotwho.rsm.event.impl.world.ChunkLoadEvent;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements Accessor {

    @Shadow
    public abstract Connection getConnection();

    @Inject(method = "sendChat(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (new PlayerChatEvent(message).post()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendCommand(String message, CallbackInfo ci) {
        if (new PlayerChatEvent(message).post()) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "handleBundlePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"
            )
    )
    private void wrapPacketHandle(Packet<?> packet, PacketListener listener, Operation<Void> original) {
        if (new PacketEvent.Receive(packet).post()) return;
        original.call(packet, listener);
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At("TAIL"))
    private void onChunkLoad(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        int x = packet.getX();
        int z = packet.getZ();

        if (Minecraft.getInstance().level == null) return;
        LevelChunk chunk = Minecraft.getInstance().level.getChunkSource().getChunk(x, z, false);
        if (chunk == null) return;
        new ChunkLoadEvent(chunk).post();
    }

    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void onHandlePlayerMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        Ether ether = RSM.getModule(Ether.class);
        if (ether == null) return;
        ether.onHandleMovePlayer(packet, getConnection(), ci);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onPostSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket, CallbackInfo ci) {
        if (mc.screen instanceof AbstractContainerScreen<?> container) {
            new GuiEvent.SlotUpdate(mc.screen, clientboundContainerSetSlotPacket, container.getMenu()).post();
        }
    }
}
