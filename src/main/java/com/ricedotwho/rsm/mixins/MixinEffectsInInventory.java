package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.location.Location;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(EffectsInInventory.class)
public class MixinEffectsInInventory {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hideInventoryPotionEffects(GuiGraphicsExtractor graphics, Collection<MobEffectInstance> activeEffects, int x0, int yStep, int mouseX, int mouseY, int maxWidth, CallbackInfo ci) {
        if (!Location.isInSkyblock()) return;
        ci.cancel();
    }
}
