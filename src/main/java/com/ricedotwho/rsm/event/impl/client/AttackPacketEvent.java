package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;

public abstract sealed class AttackPacketEvent extends Event {
    @Cancellable
    public final static class Pre extends AttackPacketEvent {

    }

    public final static class Post extends AttackPacketEvent {

    }
}
