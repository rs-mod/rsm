package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.DungeonPlayer;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.inventory.Slot;

@Getter
@ModuleInfo(aliases = "Leap Rotate Fix", id = "LeapRotateFix", category = Category.DUNGEONS)
public class LeapRotateFix extends Module {
    private static long clickedAt = 0;

    private static final NumberSetting timeout = new NumberSetting("Timeout", 0, 1000, 500, 1);

    private static Float xRot = null;
    private static Float yRot = null;

    public LeapRotateFix() {
        this.registerProperty(timeout);
    }

    @SubscribeEvent
    public void onSlotClick(GuiEvent.HandleClick event) {
        if (event.getSlotID() < 11 || event.getSlotID() > 16 || !(mc.screen instanceof AbstractContainerScreen<?> screen) || screen.getMenu().containerId != event.getContainerID()) return;
        String title = screen.getTitle().getString();
        if (!Utils.equalsOneOf(title, "Spirit Leap", "Teleport to Player")) return;
        Slot slot = screen.getMenu().getSlot(event.getSlotID());
        String name = ChatFormatting.stripFormatting(slot.getItem().getHoverName().getString()).trim().split(" ")[0];
        DungeonPlayer player = Dungeon.getPlayer(name);
        if (player == null || player.getPlayer() == null) return;
        xRot = player.getPlayer().getXRot();
        yRot = player.getPlayer().getYRot();
        clickedAt = System.currentTimeMillis();
    }


    public static void handlePlayerPositionPacketPost(ClientboundPlayerPositionPacket packet) {
        LocalPlayer player = mc.player;
        if (player == null || xRot == null || yRot == null) {
            return;
        }

        if (System.currentTimeMillis() - clickedAt > timeout.getValue().longValue()) {
            xRot = null;
            yRot = null;
            return;
        }

        var relatives = packet.relatives();
        var rotationChange = packet.change();

        var isRelativeRotations = relatives.contains(Relative.X_ROT) && relatives.contains(Relative.Y_ROT);
        var is0RotationChange = rotationChange.xRot() == 0.0f && rotationChange.yRot() == 0.0f;
        if (!isRelativeRotations || !is0RotationChange) {
            return;
        }

        player.setXRot(xRot);
        player.setYRot(yRot);

        xRot = null;
        yRot = null;
    }
}
