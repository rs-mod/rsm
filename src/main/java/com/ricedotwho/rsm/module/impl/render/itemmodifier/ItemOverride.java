package com.ricedotwho.rsm.module.impl.render.itemmodifier;

import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

@AllArgsConstructor
public class ItemOverride {
    public String name;
    public boolean enabled;
    public Color color = null;

    public ItemOverride(ItemStack stack) {
        this.name = stack.getHoverName().getString();
        this.enabled = true;

        DyedItemColor applied = stack.getComponentsPatch().get(stack.getComponents(), DataComponents.DYED_COLOR);

        if (applied != null) {
            color = Color.fromARGB(ARGB.opaque(applied.rgb()));
        }
    }

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }
}

