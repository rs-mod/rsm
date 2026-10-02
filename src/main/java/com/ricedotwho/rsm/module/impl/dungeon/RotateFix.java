package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.managers.dungeon.DungeonPlayer;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Rotate Fix", id = "leap-rotate-fix", category = Category.DUNGEONS)
public class RotateFix extends Module {
    @SuppressWarnings("unused")
    private static final RotateFix instance = new RotateFix();
    private static long clickedAt = 0;

    private final NumberSetting<Integer> timeout = new NumberSetting<>("Timeout", 1, 20, 10, 1);
    private final EnumSetSetting<Mode> mode = new EnumSetSetting<>("Mode", Mode.class, List.of());

    private static Float xRot = null;
    private static Float yRot = null;

    private static final Map<Integer, Data> BOSS_ROTATIONS = Map.of(
            1, new Data(new Vec3(-42.5, 71.5, 34.5), 180f),
            2, new Data(new Vec3(-7.5, 69.5, -28.5), 0f),
            3, new Data(new Vec3(1.5, 69.5, -27.55), 0f),
            4, new Data(new Vec3(5.5, 69.5, -20.5), 0f),
            5, new Data(new Vec3(5.5, 69.5, 0.5), 0f),
            7, new Data(new Vec3(73.5, 221.5, 14.5), 0f)
    );

    @SubscribeEvent
    public void onSlotClick(GuiEvent.HandleClick event) {
        if (!mode.contains(Mode.LEAP) || event.getSlotID() < 11 || event.getSlotID() > 16 || !(mc.screen instanceof AbstractContainerScreen<?> screen) || screen.getMenu().containerId != event.getContainerID()) return;
        String title = screen.getTitle().getString();
        if (!Utils.equalsOneOf(title, "Spirit Leap", "Teleport to Player")) return;
        Slot slot = screen.getMenu().getSlot(event.getSlotID());
        String name = ChatFormatting.stripFormatting(slot.getItem().getHoverName().getString()).trim().split(" ")[0];
        DungeonPlayer player = Dungeon.getPlayer(name);
        if (player == null) return;
        if (player.findPlayer() == null) {
            xRot = 0f;
            yRot = player.getYaw();
        } else {
            xRot = player.getPlayer().getXRot();
            yRot = player.getPlayer().getYRot();
        }
        clickedAt = EventDispatcher.getTotalWorldTime();
    }

    @SubscribeEvent
    private void onPlayerPosition(PacketEvent.MainReceivePre event, ClientboundPlayerPositionPacket packet) {
        if (!mode.contains(Mode.BOSS_ENTER) || !Location.getArea().is(Island.Dungeon) || mc.player == null) return;
        PositionMoveRotation startPos = PositionMoveRotation.of(mc.player);
        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(startPos, packet.change(), packet.relatives());
        Vec3 pos = newPos.position();

        var data = BOSS_ROTATIONS.get(Location.getFloor().getNumber());
        if (data == null || !data.pos().equals(pos)) return;
        xRot = 0f;
        yRot = data.yaw();
        clickedAt = EventDispatcher.getTotalWorldTime();
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        clickedAt = 0;
    }

    public static void handlePlayerPositionPacketPost(ClientboundPlayerPositionPacket packet) {
        LocalPlayer player = mc.player;
        if (player == null || xRot == null || yRot == null) {
            return;
        }

        if (EventDispatcher.getTotalWorldTime() - clickedAt > instance.timeout.getValue()) {
            xRot = null;
            yRot = null;
            return;
        }

        var relatives = packet.relatives();
        var rotationChange = packet.change();

        var isRelativeRotations = relatives.contains(Relative.X_ROT) && relatives.contains(Relative.Y_ROT);
        var is0RotationChange = rotationChange.xRot() == 0.0f && rotationChange.yRot() == 0.0f;
        if (!isRelativeRotations || !is0RotationChange) {
            xRot = null;
            yRot = null;
            return;
        }

        player.setXRot(xRot);
        player.setYRot(yRot);

        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of(player), packet.change(), packet.relatives());
        ((LocalPlayerAccessor) player).setYRotLast(newPos.yRot());
        ((LocalPlayerAccessor) player).setXRotLast(newPos.xRot());

        xRot = null;
        yRot = null;
    }

    private enum Mode {
        LEAP,
        BOSS_ENTER
    }

    private record Data(Vec3 pos, float yaw) {}
}
