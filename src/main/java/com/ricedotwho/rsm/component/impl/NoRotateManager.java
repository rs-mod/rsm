package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Relative;

public class NoRotateManager extends ModComponent {
    public NoRotateManager() {
        super("NoRotateManager");
    }

    @Setter
    @Getter
    private static boolean lerp = false;
    private static Float xRot = null;
    private static Float yRot = null;

    private static long lastNoRotateAction = 0;

    public static void noRotateNext() {
        lastNoRotateAction = System.currentTimeMillis();
    }

    public static void handlePlayerPositionPacketPre(ClientboundPlayerPositionPacket packet) {
        if (mc.player == null) {
            return;
        }

        if (System.currentTimeMillis() - lastNoRotateAction > Ether.getTimeout().getValue().longValue()) {
            return;
        }

        var relatives = packet.relatives();
        var rotationChange = packet.change();

        // if the rotation is relative that means that it's adding to the player's
        // rotation rather than setting it. If you add 0.0f xRot and 0.0f yRot the
        // rotation doesn't change, so we don't need to norotate.
        var isRelativeRotations = relatives.contains(Relative.X_ROT) && relatives.contains(Relative.Y_ROT);
        var is0RotationChange = rotationChange.xRot() == 0.0f && rotationChange.yRot() == 0.0f;
        if (isRelativeRotations && is0RotationChange) {
            return;
        }

        xRot = mc.player.getXRot();
        yRot = mc.player.getYRot();
    }

    public static void handlePlayerPositionPacketPost() {
        LocalPlayer player = mc.player;
        if (player == null || xRot == null || yRot == null) {
            return;
        }

        player.setXRot(xRot);
        player.setYRot(yRot);

        xRot = null;
        yRot = null;
    }

    private static void reset() {
        xRot = null;
        yRot = null;
        lerp = false;
        lastNoRotateAction = 0;
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }
}
