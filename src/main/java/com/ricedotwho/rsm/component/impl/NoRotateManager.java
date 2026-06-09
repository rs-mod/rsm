package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import lombok.Getter;
import lombok.Setter;
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

    @Setter
    @Getter
    private static boolean lerp = false;
//    private static Float xRot = null;
//    private static Float yRot = null;
//
//    private static long lastNoRotateAction = 0;

    private static final ArrayList<ClientboundPlayerPositionPacket> noRotatePackets = new ArrayList<>();

    // TODO: make this not overwrite logic
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

    public static void addPacket(ClientboundPlayerPositionPacket packet) {
        noRotatePackets.add(packet);
    }

//    public static void noRotateNext() {
//        lastNoRotateAction = System.currentTimeMillis();
//    }

//    public static void handlePlayerPositionPacketPre(ClientboundPlayerPositionPacket packet) {
//        if (mc.player == null) {
//            return;
//        }
//
//        if (System.currentTimeMillis() - lastNoRotateAction > Ether.getTimeout().getValue().longValue()) {
//            return;
//        }
//
//        var relatives = packet.relatives();
//        var rotationChange = packet.change();
//
//        // if the rotation is relative that means that it's adding to the player's
//        // rotation rather than setting it. If you add 0.0f xRot and 0.0f yRot the
//        // rotation doesn't change, so we don't need to norotate.
//        var isRelativeRotations = relatives.contains(Relative.X_ROT) && relatives.contains(Relative.Y_ROT);
//        var is0RotationChange = rotationChange.xRot() == 0.0f && rotationChange.yRot() == 0.0f;
//        if (isRelativeRotations && is0RotationChange) {
//            return;
//        }
//
//        xRot = mc.player.getXRot();
//        yRot = mc.player.getYRot();
//    }

//    public static void handlePlayerPositionPacketPost() {
//        LocalPlayer player = mc.player;
//        if (player == null || xRot == null || yRot == null) {
//            return;
//        }
//
//        player.setXRot(xRot);
//        player.setYRot(yRot);
//
//        xRot = null;
//        yRot = null;
//    }
//
//    private static void reset() {
//        xRot = null;
//        yRot = null;
//        lerp = false;
//        lastNoRotateAction = 0;
//    }

//    @SubscribeEvent
//    private void onWorldLoad(WorldEvent.Load event) {
//        reset();
//    }
}
