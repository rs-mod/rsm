package com.ricedotwho.rsm.mixins;

import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCooldowns.class)
public class MixinItemCooldowns {
    @Inject(method = "getCooldownPercent", at = @At("HEAD"), cancellable = true)
    private void disablePearlCooldown(ItemStack item, float a, CallbackInfoReturnable<Float> cir) {
        if (item.getItem() == Items.ENDER_PEARL) cir.setReturnValue(0f);
    }
}
