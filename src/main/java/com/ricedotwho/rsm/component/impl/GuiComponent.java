package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.MenuType;

public class GuiComponent extends ModComponent {
    @Getter
    private static boolean inTerminal = false;
    private OpeningContainer opening = null;

    public GuiComponent() {
        super("GuiComponent");
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

    public record OpeningContainer(int wId, int slots) {
    }
}
