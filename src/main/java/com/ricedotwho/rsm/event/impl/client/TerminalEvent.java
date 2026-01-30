package com.ricedotwho.rsm.event.impl.client;

import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;

public class TerminalEvent extends Event {
    @Getter
    public static class Open extends TerminalEvent {
        private final ClientboundOpenScreenPacket packet;
        private final TerminalType type;
        private final boolean first;
        public Open(ClientboundOpenScreenPacket packet, TerminalType type, boolean first) {
            this.packet = packet;
            this.type = type;
            this.first = first;
        }
    }

    @Getter
    public static class SetSlot extends TerminalEvent {
        private final int windowId;
        private final int slot;
        private final ItemStack stack;
        public SetSlot(int windowId, int slot, ItemStack stack) {
            this.windowId = windowId;
            this.slot = slot;
            this.stack = stack;
        }
    }

    @Getter
    public static class Close extends TerminalEvent {
        private final boolean server;
        public Close(boolean server) {
            this.server = server;
        }
    }
}
