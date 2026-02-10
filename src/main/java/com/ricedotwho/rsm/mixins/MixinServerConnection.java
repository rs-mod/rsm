package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.ConfigQOL;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinServerConnection {

    @Shadow
    private volatile @Nullable PacketListener packetListener;


    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSingleplayer() || Minecraft.getInstance().player == null) return;
        if (!(this.packetListener instanceof ServerGamePacketListenerImpl gamePacketListener)) return;

        if (RSM.getModule(ConfigQOL.class).onReceive(packet, gamePacketListener)) ci.cancel();
    }
}
