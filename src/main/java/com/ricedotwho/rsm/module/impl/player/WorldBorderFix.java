package com.ricedotwho.rsm.module.impl.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ricedotwho.rsm.mixins.MixinMinecraft;
import com.ricedotwho.rsm.mixins.MixinMultiPlayerGameMode;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;

@Getter
@ModuleInfo(aliases = "World Border Fix", id = "WorldBorderFix", category = Category.PLAYER)
public class WorldBorderFix extends Module {
    private final static WorldBorderFix instance = new WorldBorderFix();


    /**
     * @see MixinMinecraft#doWorldBorderFix(WorldBorder, BlockPos, Operation<Boolean>)
     * @see MixinMultiPlayerGameMode#doWorldBorderFixUse(WorldBorder, BlockPos, Operation<Boolean>)
     * @see MixinMultiPlayerGameMode#doWorldBorderFixStartDestroy(WorldBorder, BlockPos, Operation<Boolean>)
     * @see MixinMultiPlayerGameMode#doWorldBorderFixContinueDestroy(WorldBorder, BlockPos, Operation<Boolean>)
     * @return if its enabled
     */
    public static boolean getEnabled() {
        return instance.isEnabled();
    }
}
