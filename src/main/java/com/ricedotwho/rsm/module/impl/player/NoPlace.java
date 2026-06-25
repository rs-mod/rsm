package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.context.BlockPlaceContext;

import java.util.Set;

@Getter
@ModuleInfo(aliases = "No Place", id = "NoPlace", category = Category.PLAYER)
public class NoPlace extends Module {
    private static NoPlace INSTANCE;
    private static final Set<String> ITEMS = Set.of(
            "BOUQUET_OF_LIES",
            "FLOWER_OF_TRUTH",
            "BAT_WAND",
            "STARRED_BAT_WAND",
            "INFINITE_SPIRIT_LEAP",
            "ROYAL_PIGEON",
            "ARROW_SWAPPER",
            "JINGLE_BELLS",
            "FIRE_FREEZE_STAFF",
            "UMBERELLA"
    );

    private static final String[] ENDINGS = new String[]{
            "_POCKET_BLACK_HOLE",
            "_TUBA",
            "_POWER_ORB"
    };

    public NoPlace() {
        INSTANCE = this;
    }

    public static boolean doBlockPlace(BlockPlaceContext ctx) {
        if (!INSTANCE.isEnabled() || ctx.getPlayer() == null) return false;
        String sbId = ItemUtils.getID(ctx.getPlayer().getMainHandItem());
        String name = ChatFormatting.stripFormatting(ctx.getPlayer().getMainHandItem().getHoverName().getString()).toLowerCase();
        if (sbId.isBlank()) return false;
        return sbId.startsWith("ABIPHONE")
                || ITEMS.contains(sbId) || Utils.anyMatch(String::endsWith, sbId, ENDINGS) || DungeonUtils.isPhase(Phase7.P5) && name.contains("corrupted") &&  name.contains("relic");
    }
}
