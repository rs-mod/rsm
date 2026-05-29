package com.ricedotwho.rsm.IMixin;

import net.minecraft.network.protocol.Packet;

public interface IConnection {
    void sendPacketImmediately(Packet<?> packet);
}
