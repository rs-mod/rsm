package com.ricedotwho.rsm.event.impl.player;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Cancellable
public class PlayerChatEvent extends Event {
    private final String message;
    private final boolean command;
}
