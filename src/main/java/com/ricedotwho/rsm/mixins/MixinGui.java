package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.render.ManaStar;
import com.ricedotwho.rsm.module.impl.render.ScreenTint;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    public void renderFood(GuiGraphics guiGraphics, Player player, int i, int j, CallbackInfo ci) {
        if (ManaStar.shouldHideFood()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void onRenderHudPre(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ScreenTint.getEnabled()) ScreenTint.drawTint(guiGraphics);
    }

}
