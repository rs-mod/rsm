package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.api.Cancellable;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;

@Getter
public class PacketEvent extends Event {
	private Packet<?> packet;

	public PacketEvent(Packet<?> packet) {
		this.packet = packet;
	}

	@Cancellable
	public static class Receive extends PacketEvent {
		public Receive(Packet<?> packet) {
			super(packet);
		}
	}

	@Cancellable
	public static class Send extends PacketEvent {
		public Send(Packet<?> packet) {
			super(packet);
		}
	}
}