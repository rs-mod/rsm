package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;

public class Ping extends ModComponent {
    private static long prevTime = 0;
    @Getter
    private static float averageTPS = 20f;
    @Getter
    private static long averagePing = 0;
    @Getter
    private static long instantPing = 0;

    public Ping() {
        super("Ping");
    }

    @SubscribeEvent
    public void onTimeSet(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundSetTimePacket packet)) return;

        if (prevTime != 0) {
            averageTPS = Mth.clamp((20000f / (System.currentTimeMillis() - prevTime + 1)), 0, 20);
        }
        prevTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onPingPacket(PacketEvent.Receive event) {
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
