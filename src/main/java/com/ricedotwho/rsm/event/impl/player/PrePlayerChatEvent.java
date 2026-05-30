package com.ricedotwho.rsm.event.impl.player;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PrePlayerChatEvent extends Event {
    private String message;
    private final boolean command;
}
