package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class TerminalEvent extends Event {
    @Getter
    @AllArgsConstructor
    public static class Open extends TerminalEvent {
        private final ClientboundOpenScreenPacket packet;
        private final TerminalType type;
        private final boolean first;
    }

    @Getter
    @AllArgsConstructor
    public static class PreSetSlot extends GuiEvent {
        private final int windowId;
        private final int slot;
        private final ItemStack stack;
    }

    @Getter
    @AllArgsConstructor
    public static class PostSetSlot extends GuiEvent {
        private final Screen screen;
        private final ClientboundContainerSetSlotPacket packet;
        private final AbstractContainerMenu menu;
    }

    @Getter
    @AllArgsConstructor
    public static class Close extends TerminalEvent {
        private final boolean server;
    }
}
