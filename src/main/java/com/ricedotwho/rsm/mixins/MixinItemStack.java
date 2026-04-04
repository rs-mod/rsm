package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.command.impl.itemmodifier.ItemModifierStore;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void onGetHoverName(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(ItemModifierStore.modifyName((ItemStack) (Object) this, cir.getReturnValue()));
    }
}

