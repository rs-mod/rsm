package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.SwapManager;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 1100)
public abstract class MixinClientConnectionPost {

    @Shadow
    @Nullable BandwidthDebugMonitor bandwidthDebugMonitor;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        // Don't use Connection.sendPacket
        if (!SwapManager.onPostSendPacket(packet)) {
            ci.cancel();
        }
    }

//    @Inject(method = "connect", at = @At("HEAD"), cancellable = true)
//    private static void onConnect(InetSocketAddress inetSocketAddress, boolean bl, Connection connection, CallbackInfoReturnable<ChannelFuture> cir) {
//        Class<? extends SocketChannel> class_;
//        EventLoopGroup eventLoopGroup;
//        if (Epoll.isAvailable() && bl) {
//            class_ = EpollSocketChannel.class;
//            eventLoopGroup = Connection.NETWORK_EPOLL_WORKER_GROUP.get();
//        } else {
//            class_ = NioSocketChannel.class;
//            eventLoopGroup = Connection.NETWORK_WORKER_GROUP.get();
//        }
//
//        //InetSocketAddress proxyAddress = new InetSocketAddress("chi.socks.ipvanish.com", 1080);
//        InetSocketAddress proxyAddress = new InetSocketAddress("192.252.215.2", 4145);
//
//        ChannelFuture future = new Bootstrap().group(eventLoopGroup).handler(new ChannelInitializer<Channel>() {
//            @Override
//            protected void initChannel(Channel channel) {
//                ChannelPipeline pipeline = channel.pipeline();
//
//                // Add SOCKS5 negotiation handlers FIRST
//                pipeline.addLast(Socks5ClientEncoder.DEFAULT);
//                pipeline.addLast(new Socks5InitialResponseDecoder());
//                pipeline.addLast(new Socks5InitialRequestHandler());
//                pipeline.addLast(new Socks5CommandResponseDecoder());
//                pipeline.addLast(new Socks5CommandRequestHandler(inetSocketAddress));
//
//                try {
//                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
//                } catch (ChannelException var3) {
//                }
//
//                pipeline.addLast("timeout", new ReadTimeoutHandler(30));
//                Connection.configureSerialization(pipeline, PacketFlow.CLIENTBOUND, false, ((ConnectionAccessor) connection).getBandwidthDebugMonitor());
//                connection.configurePacketHandler(pipeline);
//            }
//        }).channel(class_).connect(proxyAddress.getAddress(), proxyAddress.getPort());
//        future.addListener(f -> {
//            if (f.isSuccess()) LOGGER.info("[SOCKS5] Connected to proxy");
//            else LOGGER.error("[SOCKS5] Failed to connect to proxy", f.cause());
//        });
//        cir.setReturnValue(future);
//    }


}
