package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.game.TerminalEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetSetting;
import com.ricedotwho.rsm.module.api.settings.impl.SaveSetting;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Term;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.MenuType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
@Register
public class Terminals implements Accessor {
    private static final DecimalFormat TWO_PLACE = new DecimalFormat("0.##");

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

    @SubscribeEvent(receiveCancelled = true)
    private void onPacket(PacketEvent.MainReceivePre event) {
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
                    Scheduler.schedule(TickEvent.ClientStart.class, () -> new GuiEvent.Loaded(mc.screen).post());
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

    public void openTermSim(ClientboundOpenScreenPacket packet, TerminalType type) {
        onTerminal(new TerminalEvent.Open(packet, type));
        inTerminal = true;
    }

    @SubscribeEvent
    private void onSendWindowClose(PacketEvent.Send event, ServerboundContainerClosePacket packet) {
        if (!inTerminal) return;
        new TerminalEvent.Close(false).post();
        inTerminal = false;
    }

    public void reset() {
        if (current != null) current.onClose();
        current = null;
        clickedAt = 0;
        inTerminal = false;
        clicks.clear();
        first = 0;
    }

    @SubscribeEvent
    private void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onTerminalClose(TerminalEvent.Close event) {
        if (event.isServer() && current.getSolution().size() < 2) { // solution is 1 or 0
            long time = (clickedAt == 0L ? System.currentTimeMillis() : clickedAt) - openedAt;
            updateBests(current.getType(), time, false);
        }
        reset();
    }

    public void onTermSimClose(boolean player) {
        if (!player) {
            long time = (clickedAt == 0L ? System.currentTimeMillis() : clickedAt) - openedAt;
            updateBests(current.getType(), time, true);
        }
        reset();
    }

    public void onTermSimOpen(TerminalType type, String title) {
        if (current == null || current.getType() != type) {
            openedAt = System.currentTimeMillis();
            current = TerminalSolver.getInstance().create(type, title);
        }
        if (current != null) current.onOpenContainer();
    }

    // this should be called after the packet is processed probably!
    @SubscribeEvent
    private void onTerminal(TerminalEvent.Open event) {
        String title = event.getPacket().getTitle().getString();
        if (current != null && (!current.isClicked() && !TerminalSolver.getInstance().getMode().is(TerminalSolver.HideClicked.ZERO_PING) || !current.getGuiTitle().equals(title)) && current.getWindowCount() <= 2) {
            reset();
        }

        if (current == null || current.getType() != event.getType()) {
            openedAt = System.currentTimeMillis();
            current = TerminalSolver.getInstance().create(event.getType(), title);
        }
        if (current != null) current.onOpenContainer();
    }

    // should only run when the packet is cancelled, just so we actually know what the terms solution is if the player is invwalking
    @Deprecated
    @SubscribeEvent
    private void onSetSlot(TerminalEvent.PreSetSlot event) {
        if (current != null && screenCancelled) current.onSlot(event.getSlot(), event.getStack());
    }

    @SubscribeEvent
    public void onSetSlot(GuiEvent.SlotUpdate event) {
        if (current != null) current.onSlot(event.getPacket().getSlot(), event.getPacket().getItem());
    }

    @SubscribeEvent
    public void onClick(PacketEvent.Send event, ServerboundContainerClickPacket packet) {
        if (!inTerminal) return;
//            long fc = System.currentTimeMillis() - openedAt;
//            if (current.getType() != TerminalType.MELODY && fc < TerminalSolver.getForcedFirstClick().getValue().longValue()) {
//                mc.getConnection().getConnection().disconnect(Component.literal("Failed first click check (" + fc + "ms)"));
//                event.setCancelled(true);
//                return;
//            }

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

    public void simulateClick() {
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

    private void updateBests(TerminalType type, long time, boolean sim) {
        SaveSetting<Map<TerminalType, Long>> setting = sim ? TerminalSolver.getInstance().getSimPersonalBests() : TerminalSolver.getInstance().getPersonalBests();
        long best = setting.getValue().get(type);
        String termName = Utils.capitalise(type.name().replace("_", " ").toLowerCase());

        MutableComponent message = null;
        boolean pb = time < best;
        if (pb) {
            setting.getValue().put(type, time);
            setting.save();

            if (TerminalSolver.getInstance().getTerminalTime().getValue()) {
                message = Component.empty()
                        .append(Component.literal("New PB! ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                        .append(Component.literal(termName).withStyle(ChatFormatting.RESET))
                        .append(Component.literal(" completed in " + NumberUtils.millisToSMS(time) + "s! "));
            }
        } else if (TerminalSolver.getInstance().getTerminalTime().getValue()) {
            message = Component.empty()
                    .append(Component.literal(termName).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" completed in " + NumberUtils.millisToSMS(time) + "s! "));
        }
        if (message != null) {

            // append the stats
            EnumSetSetting<TerminalSolver.ChatStats> stats = TerminalSolver.getInstance().getStats();
            StringBuilder sb = new StringBuilder();

            if (stats.contains(TerminalSolver.ChatStats.PERSONAL_BEST)) {
                sb.append(pb ? "Old: " : "Best: ")
                        .append(NumberUtils.millisToSMS(best))
                        .append("s");
            }

            if (stats.contains(TerminalSolver.ChatStats.AVERAGE_CLICK)) {
                double total = 0;
                for (long l : clicks) {
                    total += l;
                }

                // the avg ignores first click rn
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("Avg: ").append((int) (total / clicks.size())).append("ms");
            }

            if (stats.contains(TerminalSolver.ChatStats.FIRST_CLICK)) {
                long fc = first - openedAt;

                if (!sb.isEmpty()) sb.append(", ");
                sb.append("Fc: ").append(fc).append("ms");
            }

            if (stats.contains(TerminalSolver.ChatStats.CPS)) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("Cps: ").append(TWO_PLACE.format((clicks.size() + 1) / (time / 1000.0)));
            }

            if (!sb.isEmpty())
                message.append(Component.literal("(" + sb + ")").withStyle(ChatFormatting.DARK_GRAY));

            ChatUtils.chat(message);
        }
    }

    public record OpeningContainer(int wId, int slots) {
    }
}
