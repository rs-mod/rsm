package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

@Getter
public class ServerTickEvent extends Event {
    private final int id;
    private final long time;
    public ServerTickEvent(int id, long time) {
        this.id = id;
        this.time = time;
    }
}

