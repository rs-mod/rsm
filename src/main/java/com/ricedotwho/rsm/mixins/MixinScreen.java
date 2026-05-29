package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    protected void onRenderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!(((Object) this) instanceof AbstractContainerScreen<?>)) return; // No idea if this works but ig
        if (new GuiEvent.DrawBackground((Screen) (Object) this, context, mouseX, mouseY).post()) ci.cancel();
    }
}
