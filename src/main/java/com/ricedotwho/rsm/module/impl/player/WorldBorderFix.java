package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.mixins.MixinMinecraft;
import com.ricedotwho.rsm.mixins.MixinMultiPlayerGameMode;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;

@Getter
@ModuleInfo(aliases = "World Border Fix", id = "WorldBorderFix", category = Category.PLAYER)
public class WorldBorderFix extends Module {
    private static WorldBorderFix INSTANCE;

    public WorldBorderFix() {
        INSTANCE = this;
    }

    /**
     * @see MixinMinecraft#doWorldBorderFix(WorldBorder, BlockPos)
     * @see MixinMultiPlayerGameMode#doWorldBorderFixUse(WorldBorder, BlockPos) 
     * @see MixinMultiPlayerGameMode#doWorldBorderFixStartDestroy(WorldBorder, BlockPos) 
     * @see MixinMultiPlayerGameMode#doWorldBorderFixContinueDestroy(WorldBorder, BlockPos) 
     * @return if its enabled
     */
    public static boolean getEnabled() {
        return INSTANCE.isEnabled();
    }
}
