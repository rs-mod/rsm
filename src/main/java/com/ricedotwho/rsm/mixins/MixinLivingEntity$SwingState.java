package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.Animations;
import com.ricedotwho.rsm.type.Accessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.SwingState.class)
public abstract class MixinLivingEntity$SwingState implements Accessor {
    @Shadow
    public abstract boolean isSwinging();

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void rsm$preventReSwing(InteractionHand hand, SwingAnimation animation, int durationTicks, CallbackInfo ci) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoSwing().getValue() && this.isSwinging()) ci.cancel();
    }
}
