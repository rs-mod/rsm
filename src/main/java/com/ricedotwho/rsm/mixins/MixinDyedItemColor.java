package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.command.impl.itemmodifier.ItemModifierStore;
import com.ricedotwho.rsm.command.impl.itemmodifier.ItemNameOverride;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyedItemColor.class)
public class MixinDyedItemColor {

    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    private static void getOrDefault(ItemStack stack, int i, CallbackInfoReturnable<Integer> cir) {
        ItemNameOverride o;
        if ((o = ItemModifierStore.getData().get(ItemUtils.getUUID(stack))) != null && o.enabled && o.colour != null) {
            // ARGB
            cir.setReturnValue(o.colour.getRGB());
        }
    }
}

