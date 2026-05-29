package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;

public class AttackPacketEvent extends Event {
    @Cancellable
    public static class Pre extends AttackPacketEvent {

    }

    public static class Post extends AttackPacketEvent {

    }
}
