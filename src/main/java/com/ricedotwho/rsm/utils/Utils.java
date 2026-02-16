package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

@UtilityClass
public class Utils implements Accessor {
    public boolean equalsOneOf(Object object, Object... others) {
        for (Object obj : others) {
            if (Objects.equals(object, obj)) {
                return true;
            }
        }
        return false;
    }

    public VoxelShape getBlockShape(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        return state.getShape(mc.level, pos);
    }
}
