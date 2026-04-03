package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.EventComponent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 499)
public class MixinClientConnection {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"), cancellable = true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundPingPacket packet1 && packet1.getId() != 0) EventComponent.onServerTick(packet1.getId());
        if (new PacketEvent.Receive(packet).post()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("TAIL")
    )
    private void channelRead0Tail(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        new PacketEvent.PostReceive(packet).post();
    }

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
        private void sendPacket(Packet<?> packet, ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
        if (new PacketEvent.Send(packet).post()) {
            ci.cancel();
        }
    }
}