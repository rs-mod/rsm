package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;

public class UsePacketEvent extends Event {
    @Cancellable
    public static class Pre extends UsePacketEvent {

    }

    public static class Post extends UsePacketEvent {

    }
}
