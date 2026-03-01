package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.*;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Term;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.MenuType;

public class Terminals extends ModComponent {
    @Getter
    private static boolean inTerminal = false;
    private OpeningContainer opening = null;

    @Getter
    private static long openedAt = 0;
    @Getter
    private static long clickedAt = 0;
    @Getter
    private static Term current = null;

    public Terminals() {
        super("Terminals");
    }

    @SubscribeEvent
    public void onPacketRaw(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundOpenScreenPacket packet) {
            int slots = Utils.getGuiSlotCount(packet.getType());
            if (slots != -1) {
                opening = new OpeningContainer(packet.getContainerId(), slots);
            }

            if (!Utils.equalsOneOf(packet.getType(), MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6)) return;
            String title = packet.getTitle().getString();
            TerminalType type = TerminalType.findByStartsWithGuiName(title);
            if (!type.equals(TerminalType.NONE)) {
                new TerminalEvent.Open(packet, type).post();
                inTerminal = true;
            }
        } else if (event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (opening != null && packet.getContainerId() == opening.wId) {
                if (packet.getSlot() == opening.slots - 1) {
                    TaskComponent.onTick(0, () -> new GuiEvent.Loaded(mc.screen).post());
                    opening = null;
                }
            }

            if (!inTerminal) return;
            new TerminalEvent.PreSetSlot(packet.getContainerId(), packet.getSlot(), packet.getItem()).post();
        }
        else if (event.getPacket() instanceof ClientboundContainerClosePacket) {
            if (inTerminal) {
                new TerminalEvent.Close(true).post();
                inTerminal = false;
            }
        }
    }

    @SubscribeEvent
    public void onSendWindowClose(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundContainerClosePacket) {
            if (inTerminal) {
                new TerminalEvent.Close(false).post();
                inTerminal = false;
            }
        }
    }

    public void reset() {
        current = null;
        clickedAt = 0;
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onTerminalClose(TerminalEvent.Close event) {
        if (event.isServer() && current.getSolution().size() < 2) { // solution is 1 or 0
            long time = (clickedAt == 0L ? System.currentTimeMillis() : clickedAt) - openedAt;
            updateBests(current.getType(), time);
        }
        reset();
    }

    // this should be called after the packet is processed probably!
    @SubscribeEvent
    public void onTerminal(TerminalEvent.Open event) {
        String title = event.getPacket().getTitle().getString();
        if (current != null && (!current.isClicked() && !TerminalSolver.getMode().is("Zero Ping") || !current.getGuiTitle().equals(title)) && current.getWindowCount() <= 2) {
            // set null twice but who cares
            current = null;
            new TerminalEvent.Close(false).post();
        }

        if (current == null || current.getType() != event.getType()) {
            openedAt = System.currentTimeMillis();
            current = TerminalSolver.createTerm(event.getType(), event.getPacket().getTitle().getString());
        }
        if (current != null) current.onOpenContainer();
    }

    @SubscribeEvent
    public void onSetSlot(GuiEvent.SlotUpdate event) {
        if (current != null) current.onSlot(event.getPacket().getSlot(), event.getPacket().getItem());
    }

    @SubscribeEvent
    public void onClick(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundContainerClickPacket && Terminals.isInTerminal()) {
            clickedAt = System.currentTimeMillis();
            if (current != null) current.setClicked();
        }
    }

    private void updateBests(TerminalType type, long time) {
        long best = TerminalSolver.personalBests.get(type);
        String termName = Utils.capitalise(type.name().replace("_", " ").toLowerCase());

        if (time < best) {
            TerminalSolver.personalBests.put(type, time);
            TerminalSolver.savePersonalBests();

            if (TerminalSolver.getTerminalTime().getValue()) {
                Component message = Component.empty()
                        .append(Component.literal("New PB!").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(termName).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" completed in " + NumberUtils.millisToSMS(time) + "s! "))
                        .append(Component.literal("(Old: " + NumberUtils.millisToSMS(best) + ")").withStyle(ChatFormatting.DARK_GRAY));

                ChatUtils.chat(message);
            }
        } else if(TerminalSolver.getTerminalTime().getValue()) {
            Component message = Component.empty()
                    .append(Component.literal(termName).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" completed in " + NumberUtils.millisToSMS(time) + "s! "))
                    .append(Component.literal("(" + NumberUtils.millisToSMS(best) + ")").withStyle(ChatFormatting.DARK_GRAY));

            ChatUtils.chat(message);
        }
    }

    public record OpeningContainer(int wId, int slots) {
    }
}
