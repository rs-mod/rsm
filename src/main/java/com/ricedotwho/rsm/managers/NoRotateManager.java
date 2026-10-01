package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

import java.util.ArrayList;

import static com.ricedotwho.rsm.type.Accessor.mc;

@UtilityClass
public class NoRotateManager {

    @Setter
    @Getter
    private boolean lerp = false;

    private static final ArrayList<ClientboundPlayerPositionPacket> noRotatePackets = new ArrayList<>();

    private static Float xRot = null;
    private static Float yRot = null;

    /// This will run after the {@link PacketEvent.MainReceivePre}
    public void preMovePlayer(ClientboundPlayerPositionPacket packet) {
        if (!noRotatePackets.contains(packet)) return;
        noRotatePackets.remove(packet);
        LocalPlayer player = mc.player;
        if (player == null) return;

        var relatives = packet.relatives();
        var rotationChange = packet.change();

        var isRelatives = relatives.contains(Relative.X_ROT) && relatives.contains(Relative.Y_ROT);
        var change = rotationChange.xRot() == 0.0f && rotationChange.yRot() == 0.0f;
        if (isRelatives && change) return;

        xRot = player.getXRot();
        yRot = player.getYRot();
    }

    public void postMovePlayer(ClientboundPlayerPositionPacket packet) {
        if (xRot == null || mc.player == null) return;
        var player = mc.player;

        player.setXRot(xRot);
        player.setYRot(yRot);

        xRot = null;
        yRot = null;

        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of(player), packet.change(), packet.relatives());
        ((LocalPlayerAccessor) player).setYRotLast(newPos.yRot());
        ((LocalPlayerAccessor) player).setXRotLast(newPos.xRot());
    }

    public static void addPacket(ClientboundPlayerPositionPacket packet) {
        noRotatePackets.add(packet);
    }
}
