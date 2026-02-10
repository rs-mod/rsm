package com.ricedotwho.rsm.module;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.EtherUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Items;

@Getter
@ModuleInfo(aliases = "QOL", id = "ConfigQOL", category = Category.OTHER)
public class ConfigQOL extends Module {
    private final BooleanSetting spEther = new BooleanSetting("Singleplayer Etherwarps", true);
    private final BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);

    public ConfigQOL() {
        this.registerProperty(
                forceSkyblock,
                spEther
        );
    }

    public boolean isForceSkyblock() {
        return this.isEnabled() && Minecraft.getInstance().isSingleplayer() && forceSkyblock.getValue();
    }

    public boolean onReceive(Packet<?> packet, ServerGamePacketListenerImpl packetListener) {
        if (!this.isEnabled()) return false;
        if (!spEther.getValue()) return false;

        if (!(packet instanceof ServerboundUseItemPacket useItemPacket)) return false;
        ServerPlayer player = packetListener.getPlayer();
        if (!player.isShiftKeyDown() || player.getInventory().getSelectedItem().getItem() != Items.DIAMOND_SHOVEL) return false;

        BlockPos pos = EtherUtils.getEtherPosFromOrigin(player.position().add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d), useItemPacket.getYRot(), useItemPacket.getXRot());
        if (pos == null) {
            return false;
        }

        packetListener.teleport(pos.getX() + 0.5d, pos.getY() + 1d, pos.getZ() + 0.5d, useItemPacket.getYRot(), useItemPacket.getXRot());
        return true;
    }
}
