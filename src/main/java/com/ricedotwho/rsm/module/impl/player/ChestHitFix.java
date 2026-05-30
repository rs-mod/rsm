package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

@Getter
@ModuleInfo(aliases = "Chest Hit Fix", id = "ChestHitFix", category = Category.PLAYER)
public class ChestHitFix extends Module {
    private static final MultiBoolSetting blocks = new MultiBoolSetting("Blocks", List.of("All", "Skulls", "Chest", "Lever"), List.of("Skulls", "Chest", "Lever"));

    public ChestHitFix() {
        this.registerProperty(blocks);
    }

    public static boolean shouldRun() {
        ChestHitFix module = RSM.getModule(ChestHitFix.class);
        if (module != null && module.isEnabled()) {
            if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
                return false;
            }
            BlockState state = mc.level.getBlockState(blockHitResult.getBlockPos());
            return blocks.get("All")
                    || Utils.equalsOneOf(state.getBlock(), Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD) && blocks.get("Essence")
                    || Utils.equalsOneOf(state.getBlock(), Blocks.CHEST, Blocks.TRAPPED_CHEST) && blocks.get("Chest")
                    || state.is(Blocks.LEVER) && blocks.get("Lever");
        }
        return false;
    }
}
