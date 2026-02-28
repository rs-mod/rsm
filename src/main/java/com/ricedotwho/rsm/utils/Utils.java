package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
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

    public static int getGuiSlotCount(MenuType<?> menuType) {
        if (menuType == MenuType.GENERIC_9x1) return 9;
        if (menuType == MenuType.GENERIC_9x2) return 18;
        if (menuType == MenuType.GENERIC_9x3) return 27;
        if (menuType == MenuType.GENERIC_9x4) return 36;
        if (menuType == MenuType.GENERIC_9x5) return 45;
        if (menuType == MenuType.GENERIC_9x6) return 54;
        return -1;
    }

    public <T extends Enum<T>> T findEnumByName(Class<T> enumClass, String name, T defaultValue) {
        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return defaultValue;
        }
    }

    public String capitalise(String input) {
        StringBuilder sb = new StringBuilder();
        String[] words = input.split(" ");
        for (String word : words) {
            if(word.isEmpty()) continue;
            String first = word.substring(0, 1);
            String rest = word.substring(1);
            sb.append(first.toUpperCase()).append(rest).append(" ");
        }
        return sb.toString().trim();
    }
}
