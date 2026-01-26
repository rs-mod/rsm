package com.ricedotwho.rsm.event.impl.player;

import com.ricedotwho.rsm.event.Cancellable;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

@Getter
@Cancellable
public class PlayerChatEvent extends Event {
    private final String message;
    private final String originalMessage;

    public PlayerChatEvent(String message) {
        this.message = message;
        this.originalMessage = message;
    }
}
