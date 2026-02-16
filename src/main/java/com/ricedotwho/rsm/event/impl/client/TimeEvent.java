package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class TimeEvent extends Event {
    @Getter
    @AllArgsConstructor
    public static class Millisecond extends TimeEvent {
        private final long millis;
    }

    public static class Second extends TimeEvent {
    }

    public static class Minute extends TimeEvent {
    }
}
