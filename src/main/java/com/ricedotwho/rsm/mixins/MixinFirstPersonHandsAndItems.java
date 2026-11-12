package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ricedotwho.rsm.module.impl.player.NoBreakReset;
import com.ricedotwho.rsm.module.impl.render.Animations;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FirstPersonHandsAndItems.class)
public class MixinFirstPersonHandsAndItems {
    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Shadow
    private float offHandHeight;

    @Inject(method = "tick", at = @At("TAIL"))
    private void rsm$maintainCustomEquipHeights(CallbackInfo ci) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoEquip().getValue()) {
            this.oMainHandHeight = 1.0f;
            this.mainHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
            this.offHandHeight = 1.0f;
        }
    }

    @Inject(at = @At("RETURN"), method = "shouldInstantlyReplaceVisibleItem", cancellable = true)
    private void rsm$onShouldInstantlyReplaceVisibleItem(ItemStack currentlyVisibleItem, ItemStack expectedItem, LocalPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoEquip().getValue()) {
            cir.setReturnValue(true);
            return;
        }
        if (!cir.getReturnValue() && NoBreakReset.getInstance().isEnabled() && NoBreakReset.getInstance().getStopFlicker().getValue()) {
            cir.setReturnValue(NoBreakReset.compare(currentlyVisibleItem, expectedItem));
        }
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"))
    private float rsm$overrideAttackStrengthScale(float originalValue) {
        if (Animations.getInstance().isEnabled() && (Animations.getInstance().getNoEquip().getValue() || Animations.getInstance().getNoSwing().getValue())) return 1f;
        return originalValue;
    }
}
