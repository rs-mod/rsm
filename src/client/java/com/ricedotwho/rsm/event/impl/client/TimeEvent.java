package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.event.Event;

public class TimeEvent extends Event {
    public static class Millisecond extends TimeEvent {
        public Millisecond() {}
    }

    public static class Second extends TimeEvent {
        public Second() {}
    }

    public static class Minute extends TimeEvent {
        public Minute() {}
    }
}
