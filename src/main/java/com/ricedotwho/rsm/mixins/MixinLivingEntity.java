package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.module.impl.render.Animations;
import com.ricedotwho.rsm.type.Accessor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Slf4j
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements Accessor {
    @Shadow
    public abstract boolean isSwinging();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Inject(method = "getModifiedSwingDuration", at = @At("RETURN"), cancellable = true)
    private void rsm$disableHasteSwingSpeed(SwingAnimation animation, CallbackInfoReturnable<Integer> cir) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoHaste().getValue()) {
            cir.setReturnValue(Animations.getInstance().getSpeed().getValue());
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z", at = @At("HEAD"), cancellable = true)
    private void preventReSwing(InteractionHand hand, SwingAnimation animation, boolean sendToTrackingPlayers, CallbackInfoReturnable<Boolean> cir) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoSwing().getValue() && this.isSwinging()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    public void onSetHealth(float health, CallbackInfo ci) {
        if ((Object) this != mc.player) return;
        var max = this.getMaxHealth();
        var before = this.getHealth();
        var after = Mth.clamp(health, 0.0F, max);
        //if (before == after) return;
        float percentage = after / max;
        if (after > before) {
            new HealthChangedEvent.Heal(max, percentage, before, after).post();
        } else {
            new HealthChangedEvent.Hurt(max, percentage, before, after).post();
        }
    }
}
