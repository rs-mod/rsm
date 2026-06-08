package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.NoRotateManager;
import com.ricedotwho.rsm.component.impl.SwapManager;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerChatEvent;
import com.ricedotwho.rsm.event.impl.player.PrePlayerChatEvent;
import com.ricedotwho.rsm.event.impl.world.ChunkLoadEvent;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.module.impl.render.opsec.OpSec;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements Accessor {

    @Shadow
    public abstract Connection getConnection();

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String modifySendChat(String original) {
        PrePlayerChatEvent event = new PrePlayerChatEvent(original, false);
        event.post();
        return event.getMessage();
    }

    @Inject(method = "sendChat(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (new PlayerChatEvent(message).post()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "sendCommand", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String modifySendCommand(String original) {
        PrePlayerChatEvent event = new PrePlayerChatEvent(original, true);
        event.post();
        return event.getMessage();
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

    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", shift = At.Shift.BEFORE))
    private void onPreHandlePlayerMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        NoRotateManager.handlePlayerPositionPacketPre(packet);
    }

    @Inject(method = "handleMovePlayer", at = @At(value = "TAIL"))
    private void onHandlePlayerMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        NoRotateManager.handlePlayerPositionPacketPost();
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onPostSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket, CallbackInfo ci) {
        if (mc.player != null) {
            new GuiEvent.SlotUpdate(mc.screen, clientboundContainerSetSlotPacket, mc.player.containerMenu).post();
        }
    }

    @Inject(method = "handleSetPlayerTeamPacket", at = @At(value = "TAIL", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void onHandleSetPlayerTeam(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket, CallbackInfo ci) {
        OpSec opSec = RSM.getModule(OpSec.class);
        if (opSec == null) return;
        opSec.getServerIdHider().getValue().onPostHandleSetPlayerTeam(clientboundSetPlayerTeamPacket);
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ClientPacketListener;)V"))
    public void onHandleLogin(CallbackInfo ci) {
        SwapManager.onHandleLogin();
    }


}
