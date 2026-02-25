package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiEvent extends Event {

    @Getter
    @AllArgsConstructor
    public static class Open extends GuiEvent {
        private final Screen screen;
    }

    @Getter
    @AllArgsConstructor
    public static class Loaded extends GuiEvent {
        private final Screen screen;
    }

    @Getter
    @AllArgsConstructor
    public static class SlotClick extends GuiEvent {
        private final Screen screen;
        private final int slot;
        private final int button;
    }

    @Getter
    @AllArgsConstructor
    public static class PreSlotUpdate extends GuiEvent {
        private final Screen screen;
        private final ClientboundContainerSetSlotPacket packet;
        private final AbstractContainerMenu menu;
    }

    @Getter
    @AllArgsConstructor
    public static class PostSlotUpdate extends GuiEvent {
        private final Screen screen;
        private final ClientboundContainerSetSlotPacket packet;
        private final AbstractContainerMenu menu;
    }

    @Getter
    @AllArgsConstructor
    public static class Click extends GuiEvent {
        private final Screen screen;
        private final MouseButtonEvent input;
    }

    @Getter
    @AllArgsConstructor
    public static class Release extends GuiEvent {
        private final Screen screen;
        private final MouseButtonEvent input;
    }

    @Getter
    @AllArgsConstructor
    public static class Key extends GuiEvent {
        private final Screen screen;
        private final KeyEvent input;
    }

    @Getter
    @AllArgsConstructor
    public static class Draw extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }

    @Getter
    @AllArgsConstructor
    public static class DrawBackground extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }

    @Getter
    @AllArgsConstructor
    public static class DrawSlot extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }

    @Getter
    @AllArgsConstructor
    public static class DrawTooltip extends GuiEvent {
        private final Screen screen;
        private final GuiGraphics gfx;
        private final int mouseX;
        private final int mouseY;
    }

    @Getter
    @AllArgsConstructor
    public static class TerminalClick extends GuiEvent {
        private final Screen screen;
        private final int slot;
        private final int button;
    }
}
