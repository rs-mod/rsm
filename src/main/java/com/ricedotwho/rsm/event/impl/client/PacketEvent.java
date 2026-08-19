package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.FilterableEvent;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.ApiStatus;

@Getter
public abstract class PacketEvent extends Event implements FilterableEvent {
	private final Packet<?> packet;

	public PacketEvent(Packet<?> packet) {
		this.packet = packet;
	}

	@Override
	public Class<?> generalTypeInfo() {
		return Packet.class;
	}

	@Override
	public Object getData() {
		return getPacket();
	}

    /// You must not use this event unless you absolutely have to. see {@link MainReceivePre} or {@link MainReceivePost}
	@Cancellable
    @ApiStatus.Internal
	public static class Receive extends PacketEvent  {
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

    @Cancellable
    public static class MainReceivePre extends PacketEvent {
        public MainReceivePre(Packet<?> packet) {
            super(packet);
        }
    }

    public static class MainReceivePost extends PacketEvent {
        public MainReceivePost(Packet<?> packet) {
            super(packet);
        }
    }
}