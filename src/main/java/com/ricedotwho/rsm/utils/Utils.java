package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

@UtilityClass
public class Utils implements Accessor {
    public VoxelShape getBlockShape(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        var shape = state.getShape(mc.level, pos);
        return shape.isEmpty() ? Shapes.block() : shape;
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

    public static MenuType<?> getMenuTypeByCount(int slots) {
        if (slots == 9) return MenuType.GENERIC_9x1;
        if (slots == 18) return MenuType.GENERIC_9x2;
        if (slots == 27) return MenuType.GENERIC_9x3;
        if (slots == 36) return MenuType.GENERIC_9x4;
        if (slots == 45) return MenuType.GENERIC_9x5;
        if (slots == 54) return MenuType.GENERIC_9x6;
        throw new IllegalArgumentException("Unexpected slot count: " + slots);
    }

    @SafeVarargs
    public <T> boolean anyMatch(BiPredicate<T, T> comparator, T a, T ... b) {
        for (T t : b) {
            if (comparator.test(a, t)) return true;
        }
        return false;
    }

    public static <T> List<T> instantiate(List<Class<? extends T>> types) {
        List<T> ret = new ArrayList<>();
        for (Class<? extends T> type : types) {
            try {
                ret.add(type.getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException e) {
                RSM.getLogger().error("Failed to instantiate {}", type.getSimpleName(), e);
                ChatUtils.chat(ChatFormatting.RED + "Failed to instantiate {}", type.getSimpleName());
            }
        }
        return ret;
    }
}
