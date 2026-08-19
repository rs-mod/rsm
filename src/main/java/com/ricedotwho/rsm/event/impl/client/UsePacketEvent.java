package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;

public abstract sealed class UsePacketEvent extends Event {
    @Cancellable
    public final static class Pre extends UsePacketEvent {

    }

    public final static class Post extends UsePacketEvent {

    }
}
