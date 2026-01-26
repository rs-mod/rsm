package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Cancellable;
import com.ricedotwho.rsm.event.Event;
import net.minecraft.network.protocol.Packet;

@Cancellable
public class PacketEvent extends Event {
	public Packet<?> packet;

	public PacketEvent(Packet<?> packet) {
		this.packet = packet;
	}

	public static class Receive extends PacketEvent {
		public Receive(Packet<?> packet) {
			super(packet);
		}
	}

	public static class Send extends PacketEvent {
		public Send(Packet<?> packet) {
			super(packet);
		}
	}
}