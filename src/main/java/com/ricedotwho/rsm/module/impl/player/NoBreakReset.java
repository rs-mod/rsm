package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

@Getter
@ModuleInfo(aliases = "No Break Reset", id = "no-break-reset", category = Category.PLAYER)
public class NoBreakReset extends Module {
    @Getter
    private static final NoBreakReset instance = new NoBreakReset();

    private final BooleanSetting stopFlicker = new BooleanSetting("Stop Flicker", false);

    public static boolean compare(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        String aUuid = ItemUtils.getUUID(a);
        String bUuid = ItemUtils.getUUID(b);
        if (!aUuid.isBlank() && !bUuid.isBlank()) {
            return aUuid.equals(bUuid);
        }

        for (var comp : a.getComponents()) {
            var type = comp.type();
            if (type == DataComponents.LORE || type == DataComponents.DAMAGE || type == DataComponents.CUSTOM_DATA) continue;

            if (!comp.value().equals(b.get(type))) return false;
        }
        return true;
    }
}
