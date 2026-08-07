package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.debugchart.LocalSampleLogger;

import static com.ricedotwho.rsm.type.Accessor.mc;

@UtilityClass
@Register
public class Ping {
    private long prevTime = 0;
    @Getter
    private float averageTPS = 20f;
    @Getter
    private long averagePing = 0;
    @Getter
    private long instantPing = 0;


    @SubscribeEvent
    private void onTimeSet(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundSetTimePacket)) return;

        if (prevTime != 0) {
            averageTPS = Mth.clamp((20000f / (System.currentTimeMillis() - prevTime + 1)), 0, 20);
        }
        prevTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    private void onPingPacket(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundPongResponsePacket(long time))) return;
        instantPing = Math.max((Util.getMillis() - time), 0);

        LocalSampleLogger logger = mc.getDebugOverlay().getPingLogger();

        int sampleSize = Math.min(logger.size(), 20);

        if (sampleSize == 0) {
            averagePing = instantPing;
        } else {
            long total = 0;
            for (int i = 0; i < sampleSize; i++) {
                total += logger.get(i);
            }

            averagePing = total / sampleSize;
        }

    }
}
