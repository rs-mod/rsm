package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.managers.location.Island;
import com.ricedotwho.rsm.managers.location.Location;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.CrossCollisionBlock.*;

// TODO: the render shape for a all false bar doesn't match the collision shape atm
@Getter
@ModuleInfo(aliases = "Bar Fix", id = "bar-fix", category = Category.OTHER)
public class BarFix extends Module {
    @Getter
    private static final BarFix instance = new BarFix();

    public boolean onSyncBlockState(BlockPos pos, BlockState newState) {
        if (!this.isEnabled()) return false;
        BlockState oldState = mc.level.getBlockState(pos);
        return isBarOrWall(oldState) && isBarOrWall(newState);
    }

    private static boolean isBarOrWall(BlockState state) {
        // This includes stained-glass panes bcs they extend IronBarsBlock
        return state.getBlock() instanceof IronBarsBlock || state.getBlock() instanceof WallBlock;
    }

    public boolean isAffectingBar(BlockPos pos, BlockState state) {
        if (isBarOrWall(state)) return true;
        for (Direction dir : Direction.values()) {
            if (isBarOrWall(mc.level.getBlockState(pos.relative(dir)))) return true;
        }
        return false;
    }

    public static boolean test(BlockState state, boolean value) {
        return Location.getArea().is(Island.Dungeon) && instance.isEnabled() && isBarOrWall(state)
                && state.getValue(NORTH) == value
                && state.getValue(SOUTH) == value
                && state.getValue(EAST) == value
                && state.getValue(WEST) == value;
    }
}
