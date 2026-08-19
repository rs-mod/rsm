package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.FilterableEvent;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.ApiStatus;

@Getter
public abstract class PacketEvent<T extends Packet<?>> extends Event implements FilterableEvent {
	private final T packet;

	public PacketEvent(T packet) {
		this.packet = packet;
	}

	@Override
	public Class<?> filterType() {
		return getPacket().getClass();
	}

    /// You must not use this event unless you absolutely have to. see {@link MainReceivePre} or {@link MainReceivePost}
	@Cancellable
    @ApiStatus.Internal
	public static class Receive<T extends Packet<?>> extends PacketEvent<T>  {
		public Receive(T packet) {
			super(packet);
		}
	}

	@Cancellable
	public static class Send<T extends Packet<?>> extends PacketEvent<T> {
		public Send(T packet) {
			super(packet);
		}
	}

    @Cancellable
    public static class MainReceivePre<T extends Packet<?>> extends PacketEvent<T> {
        public MainReceivePre(T packet) {
            super(packet);
        }
    }

    public static class MainReceivePost<T extends Packet<?>> extends PacketEvent<T> {
        public MainReceivePost(T packet) {
            super(packet);
        }
    }
}