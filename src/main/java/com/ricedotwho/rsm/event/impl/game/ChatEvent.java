package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
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

    @Getter
    public static class Chat extends ChatEvent {
        private final String string;
        public Chat(Component message) {
            super(message);
            this.string = ChatFormatting.stripFormatting(message.getString());
        }
    }

    @Getter
    @Cancellable
    public static class Show extends ChatEvent {
        private final boolean overlay;
        public Show(Component message, boolean overlay) {
            super(message);
            this.overlay = overlay;
        }
    }
}
