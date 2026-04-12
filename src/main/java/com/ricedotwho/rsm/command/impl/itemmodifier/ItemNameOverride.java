package com.ricedotwho.rsm.command.impl.itemmodifier;

import com.ricedotwho.rsm.data.Colour;
import lombok.AllArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Optional;

@AllArgsConstructor
public class ItemNameOverride {
    public String name;
    public boolean enabled;
    public Colour colour = null;

    public ItemNameOverride(ItemStack stack) {
        this.name = stack.getHoverName().getString();
        this.enabled = true;

        Optional<? extends DyedItemColor> applied;
        // noinspection OptionalAssignedToNull - why do we cast a nullable object to optional?
        if ((applied = stack.getComponentsPatch().get(DataComponents.DYED_COLOR)) != null && applied.isPresent()) {
            // schizophrenia coding
            colour = new Colour(ARGB.opaque(applied.get().rgb()));
        }
    }

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }
}

