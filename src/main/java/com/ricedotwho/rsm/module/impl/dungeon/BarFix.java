package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This still has issues, but I don't feel like fixing them rn
 */
// TODO: if the player clicks on an all false bar it turns to all true, then when its updated by neighbors the all true isn't corrected properly
// TODO: the render shape for a all false bar doesn't match the collision shape atm
@Getter
@ModuleInfo(aliases = "Bar Fix", id = "bar-fix", category = Category.OTHER)
public class BarFix extends Module {
    public static BarFix INSTANCE;

    public BarFix() {
        INSTANCE = this;
    }

    public boolean onSyncBlockState(BlockPos pos, BlockState newState) {
        if (!this.isEnabled()) return false;
        BlockState oldState = mc.level.getBlockState(pos);
        return isBarOrWall(oldState) && isBarOrWall(newState);
    }

    private boolean isBarOrWall(BlockState state) {
        // This includes stained-glass panes bcs they extend IronBarsBlock
        return state.getBlock() instanceof IronBarsBlock || state.getBlock() instanceof WallBlock;
    }
}
