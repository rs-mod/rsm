package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.DungeonUtils;
import lombok.Getter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;

@Getter
@ModuleInfo(aliases = "Hide Players", id = "HidePlayers", category = Category.RENDER)
public class HidePlayers extends Module {
    private final BooleanSetting wither = new BooleanSetting("Hide Mini Wither", true);
    private final BooleanSetting players = new BooleanSetting("Hide Players Enabled", false);
    private final ModeSetting playerMode = new ModeSetting("Mode", "Device", Arrays.asList("Range", "Device", "P3"));
    private final NumberSetting distance = new NumberSetting("Distance", 0.5, 25, 5, 0.5);

    public HidePlayers() {
        this.registerProperty(
                wither,
                players,
                playerMode,
                distance
        );
    }

    public static boolean shouldHide(Entity e) {
        ChatUtils.chat("shouldHide");
        HidePlayers module = RSM.getModule(HidePlayers.class);
        if (module != null && module.isEnabled()) {
            if (e instanceof WitherBoss wither && wither.getMaxHealth() == 300F) {
                return true;
            }

            ChatUtils.chat("ShouldHide called for %s", e.getClass().getSimpleName());

            if (e instanceof Player player && module.getPlayers().getValue()) {
                boolean atDevice = ((mc.player.distanceToSqr(108.63, 120.0, 94.0) <= 1.8 || mc.player.distanceToSqr(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.isPhase(Phase7.P3));
                ChatUtils.chat("version: %s, atDev: %s", player.getUUID().version() == 4, ((!atDevice && module.getPlayerMode().getIndex() == 1)));
                if (player.getUUID().version() == 4 || player == mc.player || ((!atDevice && module.getPlayerMode().getIndex() == 1))) return false;
                if (module.getPlayerMode().getIndex() == 0) {
                    ChatUtils.chat("secondlast: %s", mc.player.distanceToSqr(player) <= module.getDistance().getValue());
                    return mc.player.distanceToSqr(player) <= module.getDistance().getValue();
                }
                ChatUtils.chat("final: %s", module.getPlayerMode().getIndex() == 2 && Dungeon.isInP3());
                return module.getPlayerMode().getIndex() == 2 && Dungeon.isInP3();
            }
        }

        return false;
    }
}
