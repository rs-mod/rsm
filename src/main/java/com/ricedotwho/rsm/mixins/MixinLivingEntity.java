package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.module.impl.render.Animations;
import com.ricedotwho.rsm.type.Accessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements Accessor {

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Inject(method = "getModifiedSwingDuration", at = @At("HEAD"), cancellable = true)
    public void rsm$disableHasteSwingSpeed(CallbackInfoReturnable<Integer> cir, @Local SwingAnimation animation) {
        if (Animations.getInstance().isEnabled()) {
            if (Animations.getInstance().getCustomSpeed().getValue()) {
                cir.setReturnValue(Animations.getInstance().getSpeed().getValue());
            } else if (Animations.getInstance().getNoHaste().getValue()) {
                cir.setReturnValue(animation.duration());
            }
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    public void rsm$onSetHealth(float health, CallbackInfo ci) {
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
