package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.network.protocol.common.ClientboundPingPacket;

@Getter
public class ServerTickEvent extends Event {
    private final ClientboundPingPacket packet;
    private final long time;
    public ServerTickEvent(ClientboundPingPacket packet, long time) {
        this.packet = packet;
        this.time = time;
    }
}

