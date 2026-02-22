package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.client.TerminalEvent;
import com.ricedotwho.rsm.event.impl.game.*;
import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.BlockChangeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.mixins.accessor.AccessorClientboundSectionBlocksUpdatePacket;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class EventComponent extends ModComponent {
    @Getter
    private static boolean inTerminal = false;
    @Getter
    private static long totalWorldTime = 0L;
    private static long clientLifeTime = 0L;

    private final ResourceLocation HUD_LAYER = ResourceLocation.fromNamespaceAndPath("rsm", "rsm_hud");

    private OpeningContainer opening = null;

    public EventComponent() {
        super("EventComponent");

        ClientPlayConnectionEvents.JOIN.register((cpl, ps, mc) -> {
            new ConnectionEvent.Connect().post();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((cpl, mc) -> {
            new ConnectionEvent.Disconnect().post();
        });

        ClientTickEvents.START_WORLD_TICK.register(a -> {
            clientLifeTime++;
            new ClientTickEvent.Start(clientLifeTime).post();
        });

        ClientTickEvents.END_WORLD_TICK.register(a -> {
            new ClientTickEvent.End(clientLifeTime).post();
        });

        WorldRenderEvents.START_MAIN.register((context) -> {
            new Render3DEvent.Start(context).post();
        });

        WorldRenderEvents.END_EXTRACTION.register((context) -> {
            new Render3DEvent.Extract(context).post();
        });

        WorldRenderEvents.END_MAIN.register((context) -> {
            new Render3DEvent.Last(context).post();
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            new WorldEvent.Load(world).post();
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            new GuiEvent.Open().post();
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, HUD_LAYER, (gfx, deltaTicks) -> new Render2DEvent(gfx, deltaTicks).post());
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
            if(!type.equals(TerminalType.NONE)) {
                // todo: fix first check
                new TerminalEvent.Open(packet, type, !inTerminal).post();
                inTerminal = true;
            }
        } else if (event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (opening != null && packet.getContainerId() == opening.wId) {
                if (packet.getSlot() > opening.slots) {
                    TaskComponent.onTick(0, () -> new GuiEvent.Loaded().post());
                    opening = null;
                }
            }

            if (!inTerminal) return;
            new TerminalEvent.SetSlot(packet.getContainerId(), packet.getSlot(), packet.getItem()).post();
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

    // is this actually better than a mixin into chunk? might be needed for our ss solver tho
    @SubscribeEvent
    public void onBlockPacket(PacketEvent.Receive event) {
        if(event.getPacket() instanceof ClientboundBlockUpdatePacket packet) {
            new BlockChangeEvent(packet.getPos(), packet.getBlockState()).post();
        } else if (event.getPacket() instanceof ClientboundSectionBlocksUpdatePacket pack) {
            AccessorClientboundSectionBlocksUpdatePacket packet = (AccessorClientboundSectionBlocksUpdatePacket) pack;
            for(int i = 0; i < packet.getPositions().length; ++i) {
                short s = packet.getPositions()[i];
                SectionPos sectionPos = packet.getSectionPos();
                BlockPos pos = new BlockPos(sectionPos.relativeToBlockX(s), sectionPos.relativeToBlockY(s), sectionPos.relativeToBlockZ(s));
                new BlockChangeEvent(pos, packet.getStates()[i]).post();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerHealthChange(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundSetHealthPacket packet)) return;
        float after = packet.getHealth();
        float before = mc.player == null ? 0f : mc.player.getHealth();
        if (after > before) {
            new HealthChangedEvent.Heal(before, after).post();
        } else {
            new HealthChangedEvent.Hurt(before, after).post();
        }
    }

    @SubscribeEvent
    public void onChatPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSystemChatPacket(
                net.minecraft.network.chat.Component content, boolean overlay
        )) {
            if (overlay) {
                new ChatEvent.ActionBar(content).post();
            } else {
                new ChatEvent.Chat(content).post();
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundPingPacket packet && packet.getId() != 0) {
            totalWorldTime++;
            new ServerTickEvent(packet.getId(), totalWorldTime).post();
        }
    }

    @SubscribeEvent
    public void onTimeUpdate(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket packet) {
            totalWorldTime = packet.gameTime();
        }
    }

    public record OpeningContainer(int wId, int slots) {
    }
}
