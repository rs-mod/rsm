package com.ricedotwho.rsm.component.impl;


import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

import java.util.ArrayList;

public class NoRotateManager extends ModComponent {
    public NoRotateManager() {
        super("NoRotateManager");
    }

    private static Float xRot = null;
    private static Float yRot = null;
    private static Float xRot0 = null;
    private static Float yRot0 = null;

    private static final ArrayList<ClientboundPlayerPositionPacket> noRotatePackets = new ArrayList<>();

    public static void saveRotations() {
        if (mc.player == null) return;
        xRot = mc.player.getXRot();
        yRot = mc.player.getYRot();
        xRot0 = mc.player.xRotO;
        yRot0 = mc.player.yRotO;
    }

    /// first needed for tp maze, as this allows me to set rotation earlier without hypixel lagging me back
    public static void handleTp(ClientboundPlayerPositionPacket packet) {
        if (!noRotatePackets.contains(packet)) return;
        noRotatePackets.remove(packet);
        LocalPlayer player = mc.player;
        if (player == null || xRot == null) return;
        player.setXRot(xRot);
        player.setYRot(yRot);
        player.setOldRot(yRot0, xRot0);
        ((LocalPlayerAccessor) player).setYRotLast(yRot0);
        ((LocalPlayerAccessor) player).setXRotLast(xRot0);
        reset();
    }

    private static void reset() {
        xRot = null;
        yRot = null;
        xRot0 = null;
        yRot0 = null;
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        noRotatePackets.clear();
    }

    public static void addPacket(ClientboundPlayerPositionPacket packet) {
        noRotatePackets.add(packet);
    }
}
