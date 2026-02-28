package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class GuiEvent extends Event {

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Open extends GuiEvent {
        private final Screen screen;
    }

    @Getter
    @AllArgsConstructor
    public static class Close extends GuiEvent {
        private final Screen screen;
    }

    @Getter
    @AllArgsConstructor
    public static class Loaded extends GuiEvent {
        private final Screen screen;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class SlotClick extends GuiEvent {
        private final Screen screen;
        private final int slot;
        private final int button;
    }

    @Getter
    @AllArgsConstructor
    public static class SlotUpdate extends GuiEvent {
        private final Screen screen;
        private final ClientboundContainerSetSlotPacket packet;
        private final AbstractContainerMenu menu;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Click extends GuiEvent {
        private final Screen screen;
        private final MouseButtonEvent input;
        private final boolean doubled;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Release extends GuiEvent {
        private final Screen screen;
        private final MouseButtonEvent input;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Key extends GuiEvent {
        private final Screen screen;
        private final KeyEvent input;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class Draw extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class DrawBackground extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class DrawSlot extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final Slot slot;
    }

    @Getter
    @AllArgsConstructor
    @Cancellable
    public static class DrawTooltip extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }
}
