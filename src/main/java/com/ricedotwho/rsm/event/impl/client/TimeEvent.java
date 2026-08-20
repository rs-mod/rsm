package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract sealed class TimeEvent extends Event {
    @Getter
    @AllArgsConstructor
    public final static class Millisecond extends TimeEvent {
        private final long millis;
    }
}
