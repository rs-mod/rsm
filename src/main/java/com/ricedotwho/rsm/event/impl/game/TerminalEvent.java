package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;

public sealed abstract class TerminalEvent extends Event {
    @Getter
    @AllArgsConstructor
    public final static class Open extends TerminalEvent {
        private final ClientboundOpenScreenPacket packet;
        private final TerminalType type;
    }

    @Getter
    @AllArgsConstructor
    public final static class PreSetSlot extends TerminalEvent {
        private final int windowId;
        private final int slot;
        private final ItemStack stack;
        private final PacketEvent event;
    }

    @Getter
    @AllArgsConstructor
    public final static class Close extends TerminalEvent {
        private final boolean server;
    }
}
