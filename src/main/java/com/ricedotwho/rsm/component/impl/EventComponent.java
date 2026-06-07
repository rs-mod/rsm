package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.ConnectionEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.BlockChangeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.mixins.accessor.AccessorClientboundSectionBlocksUpdatePacket;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;

public class EventComponent extends ModComponent {
    @Getter
    private static long totalWorldTime = 0L;
    private static long clientLifeTime = 0L;
    @Getter
    private boolean canRender2D = false;

    private final Identifier HUD_LAYER = Identifier.fromNamespaceAndPath("rsm", "rsm_hud");

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

        ClientReceiveMessageEvents.ALLOW_GAME.register((text, overlay) -> !new ChatEvent.Show(text, overlay).post());

        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, HUD_LAYER, (gfx, deltaTicks) -> {
            if (canRender2D) {
                new Render2DEvent(gfx, deltaTicks).post();
            }
        });
    }

    // is this actually better than a mixin into chunk? might be needed for our ss solver tho
    @SubscribeEvent
    private void onBlockPacket(PacketEvent.Receive event) {
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
    private void onPlayerHealthChange(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundSetHealthPacket packet) || mc.player == null) return;
        float after = packet.getHealth();
        float before = mc.player.getHealth();
        if (before == after) return;
        float totalHealth = mc.player.getMaxHealth();
        float percentage = after / totalHealth;
        if (after > before) {
            new HealthChangedEvent.Heal(totalHealth, percentage, before, after).post();
        } else {
            new HealthChangedEvent.Hurt(totalHealth, percentage, before, after).post();
        }
    }

    @SubscribeEvent
    private void onChatPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSystemChatPacket(Component content, boolean overlay)) {
            if (overlay) {
                new ChatEvent.ActionBar(content).post();
            } else {
                new ChatEvent.Chat(content).post();
            }
        }
    }

    @SubscribeEvent
    private void onTimeUpdate(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket packet) {
            totalWorldTime = packet.gameTime();
        }
    }

    // freaky
    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        if (!canRender2D) {
            Scheduler.tick(40, () -> canRender2D = true);
        }
    }

    public static void onServerTick(int id) {
        totalWorldTime++;
        new ServerTickEvent(id, totalWorldTime).post();
    }
}
