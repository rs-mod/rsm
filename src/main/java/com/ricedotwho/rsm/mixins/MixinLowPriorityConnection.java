package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.IMixin.IConnection;
import com.ricedotwho.rsm.component.impl.PacketOrderManager;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 380) // RSA is at 400
public abstract class MixinLowPriorityConnection implements IConnection {
    @Shadow
    protected abstract void sendPacket(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean bl);

    // This gets called earlier, before other hooks hopefully and isin't triggered by receivePacket
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"), cancellable = true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketOrderManager.onPreReceivePacket(packet); // Packet may get canceled by velocity buffer
    }


    @Override
    public void sendPacketImmediately(Packet<?> packet) {
        this.sendPacket(packet, null, true);
    }
}
