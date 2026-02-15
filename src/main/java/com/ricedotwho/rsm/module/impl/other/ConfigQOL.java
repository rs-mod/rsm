package com.ricedotwho.rsm.module.impl.other;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.EtherUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
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

    @SubscribeEvent
    public void onServerBoundChat(PacketEvent.Send event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(event.getPacket() instanceof ServerboundChatCommandPacket(String command))) return;

        if (command.startsWith("joindungeon") || command.startsWith("warp")) {
            return;
        }
        
        String commandLower = command.toLowerCase();
        
        // not master mode
        switch (commandLower) {
            case String s when s.equals("f7") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs 7");
            }
            case String s when s.equals("f6") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs 6");
            }
            case String s when s.equals("f5") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs 5");
            }
            case String s when s.equals("f4") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs 4");
            }
            case String s when s.equals("f3") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs 3");
            }
            case String s when s.equals("f2") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs 2");
            }
            case String s when s.equals("f1") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon catacombs entrance");
            }

            // Mastermode
            case String s when s.equals("m7") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 7");
            }
            case String s when s.equals("m6") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 6");
            }
            case String s when s.equals("m5") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 5");
            }
            case String s when s.equals("m4") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 4");
            }
            case String s when s.equals("m3") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 3");
            }
            case String s when s.equals("m2") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 2");
            }
            case String s when s.equals("m1") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon master_catacombs 1");
            }
            // Kuudra Tiers
            case String s when s.equals("t1") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon kuudra_normal");
            }
            case String s when s.equals("t2") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon kuudra_hot");
            }
            case String s when s.equals("t3") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon kuudra_burning");
            }
            case String s when s.equals("t4") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon kuudra_fiery");
            }
            case String s when s.equals("t5") -> {
                event.setCancelled(true);
                player.connection.sendCommand("joindungeon kuudra_infernal");
            }

            // Dungeon hub
            case String s when s.equals("dn") || s.equals("d") -> {
                event.setCancelled(true);
                player.connection.sendCommand("warp dungeon_hub");
            }
            default -> {}
        }
    }
}
