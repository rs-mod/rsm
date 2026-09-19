package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ricedotwho.rsm.module.impl.render.Animations;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Shadow
    public boolean swinging;

    @ModifyExpressionValue(method = "updateSwingTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getCurrentSwingDuration()I"))
    private int modifySwingDuration(int original) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoHaste().getValue()) return Animations.getInstance().getSpeed().getValue();
        return original;
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void preventReSwing(InteractionHand hand, CallbackInfo ci) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoSwing().getValue() && this.swinging) ci.cancel();
    }
}
