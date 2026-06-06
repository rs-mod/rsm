package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.ManaStar;
import com.ricedotwho.rsm.module.impl.render.ScreenTint;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
    public void renderFood(GuiGraphicsExtractor graphics, Player player, int i, int j, CallbackInfo ci) {
        if (ManaStar.shouldHideFood()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At(value = "HEAD"))
    public void onRenderHudPre(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ScreenTint.getEnabled()) ScreenTint.drawTint(graphics);
    }

}
