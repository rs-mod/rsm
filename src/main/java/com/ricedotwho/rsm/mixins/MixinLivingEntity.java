package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ricedotwho.rsm.event.impl.player.HealthChangedEvent;
import com.ricedotwho.rsm.module.impl.render.Animations;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
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
    public boolean swinging;

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @ModifyExpressionValue(method = "updateSwingTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getCurrentSwingDuration()I"))
    private int modifySwingDuration(int original) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoHaste().getValue()) return Animations.getInstance().getSpeed().getValue();
        return original;
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void preventReSwing(InteractionHand hand, CallbackInfo ci) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoSwing().getValue() && this.swinging) ci.cancel();
    }

    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    public void rsm$disableHasteSwingSpeed(CallbackInfoReturnable<Integer> cir) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoHaste().getValue()) {
            cir.setReturnValue(Animations.getInstance().getSpeed().getValue());
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    public void onSetHealth(float health, CallbackInfo ci) {
        if ((Object) this != mc.player) return;
        var max = this.getMaxHealth();
        var before = this.getHealth();
        var after = Mth.clamp(health, 0.0F, max);
        //if (before == after) return;
        ChatUtils.dev("hp change max {} before {} after {}", max, before, after);
        float percentage = after / max;
        if (after > before) {
            new HealthChangedEvent.Heal(max, percentage, before, after).post();
        } else {
            new HealthChangedEvent.Hurt(max, percentage, before, after).post();
        }
    }
}
