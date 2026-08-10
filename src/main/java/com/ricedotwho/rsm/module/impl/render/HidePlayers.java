package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.Phase7;
import com.ricedotwho.rsm.managers.dungeon.map.handler.Dungeon;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Hide", id = "HideEntity", category = Category.RENDER)
public class HidePlayers extends Module {
    protected static HidePlayers instance = new HidePlayers();
    private final BooleanSetting wither = new BooleanSetting("Hide Mini Wither", true);
    private final BooleanSetting players = new BooleanSetting("Hide Players", false);
    private final ModeSetting playerMode = new ModeSetting("Mode", "Device", Arrays.asList("Range", "Device", "P3"), players::getValue);
    private final NumberSetting distance = new NumberSetting("Distance", 0.5, 25, 5, 0.5, players::getValue);
    private final BooleanSetting hideDying = new BooleanSetting("Hide Dying", false);
    private final BooleanSetting hideRagnarok = new BooleanSetting("Hide Bers Ragnarok", false);
    private final BooleanSetting hideNonStarredNameTags = new BooleanSetting("Hide Non starred nametags", false);
    private final BooleanSetting hideUselessNametags = new BooleanSetting("Hide useless nametags", false);

    private static final Pattern MOB = Pattern.compile("^(?:§.\\[§.Lv\\d+§.] §.+ (?:§.)+0§f/.+|.+) (?:§.)+0§c❤$");
    private static final Map<String, Boolean> STARRED_CACHE = new HashMap<>();

    private static final String RAGNAROK = "ewogICJ0aW1lc3RhbXAiIDogMTYwNzcxNTU2Njg3NywKICAicHJvZmlsZUlkIiA6ICIwZjczMDA3NjEyNGU0NGM3YWYxMTE1NDY5YzQ5OTY3OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJPcmVfTWluZXIxMjMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZkZGU3NGIyZmRmNzc4N2M3NWQ3MWQ1MmNkZmJmZjA1MzBiMzI5ZDZiMTNlNzQxNGZiZTM4OTZjOTYwMzYwMSIKICAgIH0KICB9Cn0=";

    public static boolean shouldHide(Entity e) {
        if (instance.isEnabled() && mc.player != null) {
            if (e instanceof WitherBoss wither && wither.getMaxHealth() == 300F) {
                return true;
            }

            if (Location.getArea().is(Island.Dungeon)) {
                if (instance.hideRagnarok.getValue() && DungeonUtils.isPhase(Phase7.P3) && isRagnarok(e)) {
                    return true;
                }

                if (instance.hideNonStarredNameTags.getValue() && e.hasCustomName()) {
                    String name = ChatFormatting.stripFormatting(e.getCustomName().getString()).trim();
                    boolean r = STARRED_CACHE.computeIfAbsent(name, k -> MOB.matcher(k).find());
                    if (r) return true;
                }
            }

            if (e instanceof Player player && instance.getPlayers().getValue() && player.getUUID().version() == 4 && player != mc.player) {
                return switch (instance.getPlayerMode().getIndex()) {
                    case 0 -> {
                        double dist = instance.getDistance().getValue().doubleValue();
                        yield player.distanceToSqr(mc.player) <= dist * dist;
                    }
                    case 1 -> (mc.player.distanceToSqr(108.63, 120.0, 94.0) <= 1.8 || mc.player.distanceToSqr(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.isPhase(Phase7.P3);
                    case 2 -> Dungeon.isInP3();
                    default -> false;
                };
            }

            if (instance.getHideDying().getValue()) {
                if (e instanceof LivingEntity living && living.isDeadOrDying()) return true;
                if (e instanceof ArmorStand stand) {
                    Entity owner = stand.level().getEntity(stand.getId() - 1);
                    return owner instanceof LivingEntity living && living.isDeadOrDying();
                }
            }
        }

        return false;
    }

    protected static boolean isRagnarok(Entity e) {
        if (e instanceof Zombie z && z.isBaby()) {
            ItemStack helmet = z.getItemBySlot(EquipmentSlot.HEAD);
            return RAGNAROK.equals(ItemUtils.getTexture(helmet));
        } else if (e instanceof ArmorStand stand && stand.hasCustomName() && mc.player != null) {
            String name = ChatFormatting.stripFormatting(e.getCustomName().getString());
            return name.startsWith("Ragnarok") && name.endsWith("❤") || name.equals(mc.player.getName().getString());
        }
        return false;
    }
}
