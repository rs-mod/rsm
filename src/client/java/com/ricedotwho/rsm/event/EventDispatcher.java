package com.ricedotwho.rsm.event;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;

public class EventDispatcher {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEvent(Event event) {
        RSM.getInstance().getEventBus().post(event);
    }

    @SubscribeEvent
    public void onNetworkEvent(ClientConnectedToServerEvent event) {
        NetworkManager manager = event.manager;

        manager.channel().pipeline().addAfter("fml:packet_handler", "rsm_packet_handler", new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof Packet) {
                    RSM.getInstance().getEventBus().post(new RawPacketEvent((Packet<?>) msg));
                }
                ctx.fireChannelRead(msg);
            }
        });
    }

//    @SubscribeEvent
//    public void onPacketRaw(PacketEvent.ReceivedRaw event) {
//        if (event.packet instanceof S2DPacketOpenWindow) {
//            S2DPacketOpenWindow packet = (S2DPacketOpenWindow) event.packet;
//            String title = packet.getWindowTitle().getUnformattedText();
//            TerminalType type = TerminalType.findByStartsWithGuiName(title);
//            if(!type.equals(TerminalType.NONE)) {
//                if (!TerminalListener.inTerminal()) {
//                    Utils.postAndCatch(new TermPacketEvent.FirstOpen(packet, type));
//                }
//                Utils.postAndCatch(new TermPacketEvent.Open(packet, type));
//            }
//        } else if (event.packet instanceof S2FPacketSetSlot) {
//            S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;
//            Utils.postAndCatch2(new TermPacketEvent.SetSlot(packet.func_149175_c(), packet.func_149173_d(), packet.func_149174_e()));
//        }
//    }
//
//    @SubscribeEvent
//    public void onBlockPacket(PacketEvent.Received event) {
//        if(event.packet instanceof S23PacketBlockChange) {
//            S23PacketBlockChange packet = (S23PacketBlockChange) event.packet;
//            Utils.postAndCatch(new BlockChangedEvent(packet.getBlockPosition(), packet.getBlockState(), mc.theWorld.getBlockState(packet.getBlockPosition())));
//        } else if (event.packet instanceof S22PacketMultiBlockChange) {
//            S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange) event.packet;
//            for (S22PacketMultiBlockChange.BlockUpdateData update : packet.getChangedBlocks()) {
//                Utils.postAndCatch(new BlockChangedEvent(update.getPos(), update.getBlockState(), mc.theWorld.getBlockState(update.getPos())));
//            }
//        }
//    }
}