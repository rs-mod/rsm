package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.game.TerminalEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Term;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.MenuType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Terminals extends ModComponent {
    private static final DecimalFormat twoPlace = new DecimalFormat("0.##");

    @Getter
    private static boolean inTerminal = false;
    @Getter
    private static boolean screenCancelled = false;
    private OpeningContainer opening = null;

    @Getter
    private static long openedAt = 0;
    @Getter
    private static long clickedAt = 0;

    private static final List<Long> clicks = new ArrayList<>();
    private static long first = 0;

    @Getter
    private static Term current = null;

    public Terminals() {
        super("Terminals");
    }

    @SubscribeEvent(receiveCancelled = true)
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundOpenScreenPacket packet) {
            int slots = Utils.getGuiSlotCount(packet.getType());
            if (slots != -1) {
                opening = new OpeningContainer(packet.getContainerId(), slots);
            }

            if (Utils.equalsOneOf(packet.getType(), MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6)) {
                String title = packet.getTitle().getString();
                TerminalType type = TerminalType.findByStartsWithGuiName(title);
                if (!type.equals(TerminalType.NONE)) {
                    new TerminalEvent.Open(packet, type).post();
                    inTerminal = true;
                } else {
                    reset();
                }
            } else {
                reset();
            }

            screenCancelled = event.isCancelled();
        } else if (event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (opening != null && packet.getContainerId() == opening.wId) {
                if (packet.getSlot() == opening.slots - 1) {
                    TaskComponent.onTick(0, () -> new GuiEvent.Loaded(mc.screen).post());
                    opening = null;
                }
            }

            if (!inTerminal) return;
            new TerminalEvent.PreSetSlot(packet.getContainerId(), packet.getSlot(), packet.getItem(), event).post();
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
        inTerminal = false;
        clicks.clear();
        first = 0;
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
            reset();
        }

        if (current == null || current.getType() != event.getType()) {
            openedAt = System.currentTimeMillis();
            current = TerminalSolver.createTerm(event.getType(), event.getPacket().getTitle().getString());
        }
        if (current != null) current.onOpenContainer();
    }

    // should only run when the packet is cancelled, just so we actually know what the terms solution is if the player is invwalking
    @SubscribeEvent
    public void onSetSlot(TerminalEvent.PreSetSlot event) {
        if (current != null && event.getEvent().isCancelled()) current.onSlot(event.getSlot(), event.getStack());
    }

    @SubscribeEvent
    public void onSetSlot(GuiEvent.SlotUpdate event) {
        if (current != null) current.onSlot(event.getPacket().getSlot(), event.getPacket().getItem());
    }

    @SubscribeEvent
    public void onClick(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundContainerClickPacket && inTerminal) {
            if (current.getType() != TerminalType.MELODY && System.currentTimeMillis() - openedAt < TerminalSolver.getForcedFirstClick().getValue().longValue()) {
                event.setCancelled(true);
                return;
            }

            long now = System.currentTimeMillis();
            if (first == 0) {
                first = now;
            } else {
                clicks.add(now - clickedAt);
            }
            clickedAt = now;

            clickedAt = System.currentTimeMillis();
            if (current != null) current.setClicked();
        }
    }

    private void updateBests(TerminalType type, long time) {
        long best = TerminalSolver.getPersonalBests().getValue().get(type);
        String termName = Utils.capitalise(type.name().replace("_", " ").toLowerCase());

        MutableComponent message = null;
        boolean pb = time < best;
        if (pb) {
            TerminalSolver.getPersonalBests().getValue().put(type, time);
            TerminalSolver.savePersonalBests();

            if (TerminalSolver.getTerminalTime().getValue()) {
                message = Component.empty()
                        .append(Component.literal("New PB! ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                        .append(Component.literal(termName).withStyle(ChatFormatting.RESET))
                        .append(Component.literal(" completed in " + NumberUtils.millisToSMS(time) + "s! "));
            }
        } else if(TerminalSolver.getTerminalTime().getValue()) {
            message = Component.empty()
                    .append(Component.literal(termName).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" completed in " + NumberUtils.millisToSMS(time) + "s! "));
        }
        if (message != null) {

            // append the stats
            MultiBoolSetting stats = TerminalSolver.getStats();
            StringBuilder sb = new StringBuilder();

            if (stats.get("Personal Best")) {
                sb.append(pb ? "Old: " : "Best: ")
                        .append(NumberUtils.millisToSMS(best))
                        .append("s");
            }

            if (stats.get("Average Click")) {
                double total = 0;
                for (long l : clicks) {
                    total += l;
                }

                // the avg ignores first click rn
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("Avg: ").append((int) (total / clicks.size())).append("ms");
            }

            if (stats.get("First Click")) {
                long fc = first - openedAt;

                if (!sb.isEmpty()) sb.append(", ");
                sb.append("Fc: ").append(fc).append("ms");
            }

            if (stats.get("CPS")) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("Cps: ").append(twoPlace.format((clicks.size() + 1) / (time / 1000.0)));
            }

            if (!sb.isEmpty())
                message.append(Component.literal("(" + sb + ")").withStyle(ChatFormatting.DARK_GRAY));

            ChatUtils.chat(message);
        }
    }

    public record OpeningContainer(int wId, int slots) {
    }
}
