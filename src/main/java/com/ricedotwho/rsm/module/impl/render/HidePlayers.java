package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.DungeonUtils;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;

@Getter
@ModuleInfo(aliases = "Hide", id = "HideEntity", category = Category.RENDER)
public class HidePlayers extends Module {
    private final BooleanSetting wither = new BooleanSetting("Hide Mini Wither", true);
    private final BooleanSetting players = new BooleanSetting("Hide Players", false);
    private final ModeSetting playerMode = new ModeSetting("Mode", "Device", Arrays.asList("Range", "Device", "P3"), players::getValue);
    private final NumberSetting distance = new NumberSetting("Distance", 0.5, 25, 5, 0.5, players::getValue);
    private final BooleanSetting hideDying = new BooleanSetting("Hide Dying", false);

    public HidePlayers() {
        this.registerProperty(
                wither,
                players,
                playerMode,
                distance,
                hideDying
        );
    }

    public static boolean shouldHide(Entity e) {
        HidePlayers module = RSM.getModule(HidePlayers.class);
        if (module != null && module.isEnabled() && mc.player != null) {
            if (e instanceof WitherBoss wither && wither.getMaxHealth() == 300F) {
                return true;
            }

            if (e instanceof Player player && module.getPlayers().getValue() && player.getUUID().version() == 4 && player != mc.player) {
                return switch (module.getPlayerMode().getIndex()) {
                    case 0 -> {
                        double dist = module.getDistance().getValue().doubleValue();
                        yield player.distanceToSqr(mc.player) <= dist * dist;
                    }
                    case 1 -> (mc.player.distanceToSqr(108.63, 120.0, 94.0) <= 1.8 || mc.player.distanceToSqr(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.isPhase(Phase7.P3);
                    case 2 -> Dungeon.isInP3();
                    default -> false;
                };
            }

            if (module.getHideDying().getValue()) {
                if (e instanceof LivingEntity living && living.isDeadOrDying()) return true;
                if (e instanceof ArmorStand stand) {
                    Entity owner = stand.level().getEntity(stand.getId() - 1);
                    return owner instanceof LivingEntity living && living.isDeadOrDying();
                }
            }
        }

        return false;
    }
}
