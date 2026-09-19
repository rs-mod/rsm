package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.HideRecipeBook;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class MixinAbstractRecipeBookScreen {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen;initButton()V"), cancellable = true)
    private void hideRecipeBook(CallbackInfo ci) {
        if (!HideRecipeBook.getInstance().isEnabled()) return;
        ci.cancel();
    }
}
