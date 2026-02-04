package com.ricedotwho.rsm.event.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.client.input.KeyEvent;

@Getter
public class KeyInputEvent extends Event {
    private final InputConstants.Key key;
    private final KeyEvent keyEvent;
    private final State state;

    public KeyInputEvent(State state, KeyEvent event) {
        this.state = state;
        this.keyEvent = event;
        this.key = InputConstants.getKey(event);
    }

    public boolean isDown() {
        return this.state != State.RELEASE;
    }

    public static class Release extends KeyInputEvent {
        public Release(KeyEvent event) {
            super(State.RELEASE, event);
        }
    }

    public static class Press extends KeyInputEvent {
        public Press(KeyEvent event) {
            super(State.PRESS, event);
        }
    }

    public static class Repeat extends KeyInputEvent {
        public Repeat(KeyEvent event) {
            super(State.REPEAT, event);
        }
    }

    @Getter
    public enum State {
        RELEASE(0),
        PRESS(1),
        REPEAT(2);

        private final int index;

        State(int index) {
            this.index = index;
        }

        public static State get(int index) {
            for (State s : State.values()) {
                if (s.getIndex() == index) return s;
            }
            return null;
        }
    }
}
