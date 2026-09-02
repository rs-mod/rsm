package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerChatEvent;
import com.ricedotwho.rsm.event.impl.player.PrePlayerChatEvent;
import com.ricedotwho.rsm.event.impl.world.ChunkLoadEvent;
import com.ricedotwho.rsm.managers.NoRotateManager;
import com.ricedotwho.rsm.module.impl.dungeon.BarFix;
import com.ricedotwho.rsm.module.impl.dungeon.LeapRotateFix;
import com.ricedotwho.rsm.module.impl.dungeon.puzzle.TicTacToe;
import com.ricedotwho.rsm.module.impl.render.opsec.OpSec;
import com.ricedotwho.rsm.type.Accessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.level.block.CrossCollisionBlock.*;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements Accessor {

    @Shadow
    public abstract Connection getConnection();

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String modifySendChat(String content) {
        PrePlayerChatEvent event = new PrePlayerChatEvent(content, false);
        event.post();
        return event.getMessage();
    }

    @Inject(method = "sendChat(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String content, CallbackInfo ci) {
        if (new PlayerChatEvent(content, false).post()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "sendCommand", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String modifySendCommand(String command) {
        PrePlayerChatEvent event = new PrePlayerChatEvent(command, true);
        event.post();
        return event.getMessage();
    }

    @Inject(method = "sendCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendCommand(String command, CallbackInfo ci) {
        if (new PlayerChatEvent(command, true).post()) {
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
        boolean bl = new PacketEvent.Receive(packet).post();
        Event e = new PacketEvent.MainReceivePre(packet);
        e.setCancelled(bl);
        bl = e.post();
        if (bl) return;
        original.call(packet, listener);
        new PacketEvent.MainReceivePost(packet).post();
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
    private void onPreHandlePlayerMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        NoRotateManager.handleTp(packet, getConnection(), ci);
    }

    @Inject(method = "handleMovePlayer", at = @At(value = "TAIL"))
    private void onHandlePlayerMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        LeapRotateFix.handlePlayerPositionPacketPost(packet);
    }

    @Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundContainerSetSlotPacket;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private void onPostSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (mc.player != null) {
            new GuiEvent.SlotUpdate(mc.screen, packet, mc.player.containerMenu).post();
        }
    }

    @Inject(method = "handleSetPlayerTeamPacket", at = @At(value = "TAIL", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void onHandleSetPlayerTeam(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        OpSec.getInstance().getServerIdHider().getValue().onPostHandleSetPlayerTeam(packet);
    }

    @Inject(method = "handleBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setServerVerifiedBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onHandleBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        var module = BarFix.getInstance();
        if (module.isEnabled()) {
            BlockState after = packet.getBlockState();
            if (after.is(Blocks.AIR) || !module.isAffectingBar(packet.getPos(), after)) return;

            if (BarFix.test(after, true)) {
                after = after.setValue(NORTH, false)
                        .setValue(SOUTH, false)
                        .setValue(WEST, false)
                        .setValue(EAST, false);
            }

            ci.cancel();
            assert mc.level != null;
            mc.level.setBlock(packet.getPos(), after, Block.UPDATE_ALL);
        }
    }

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    void handleSetEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        TicTacToe.onSetEntityData(packet.id());
    }
}
