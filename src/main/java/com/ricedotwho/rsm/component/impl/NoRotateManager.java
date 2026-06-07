package com.ricedotwho.rsm.component.impl;


import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    public static void handleTp(ClientboundPlayerPositionPacket packet, Connection connection, CallbackInfo ci) {
        if (!noRotatePackets.contains(packet)) return;
        noRotatePackets.remove(packet);

        LocalPlayer player = mc.player;
        if (player == null) return;

        PositionMoveRotation startPos = PositionMoveRotation.of(player);
        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(startPos, packet.change(), packet.relatives());

        player.setPos(newPos.position());
        player.setDeltaMovement(newPos.deltaMovement());

        PositionMoveRotation oldPlayerPos = new PositionMoveRotation(player.oldPosition(), Vec3.ZERO, player.yRotO, player.xRotO);
        PositionMoveRotation newOldPlayerPos = PositionMoveRotation.calculateAbsolute(oldPlayerPos, packet.change(), packet.relatives());

        player.setOldPosAndRot(newOldPlayerPos.position(), player.yRotO, player.xRotO); // i would prefer to just set position here, but fun is private

        connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), newPos.yRot(), newPos.xRot(), false, false));

        ((LocalPlayerAccessor) player).setYRotLast(newPos.yRot());
        ((LocalPlayerAccessor) player).setXRotLast(newPos.xRot());

        ci.cancel();
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
