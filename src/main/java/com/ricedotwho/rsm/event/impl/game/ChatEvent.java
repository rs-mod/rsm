package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.Component;

@Getter
@AllArgsConstructor
public class ChatEvent extends Event {
    private final Component message;

    public static class ActionBar extends ChatEvent {
        public ActionBar(Component message) {
            super(message);
        }
    }

    public static class Chat extends ChatEvent {
        public Chat(Component message) {
            super(message);
        }
    }
}
