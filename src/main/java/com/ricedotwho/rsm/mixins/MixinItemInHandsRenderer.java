package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ricedotwho.rsm.module.impl.render.Animations;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class MixinItemInHandsRenderer {

    @Shadow
    protected abstract void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue);

    // Animations garbage, ty odin

    @WrapOperation(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    ordinal = 0
            )
    )
    private void applyCustomHandAnimation(PoseStack instance, Operation<Void> original, @Local(argsOnly = true) InteractionHand hand, @Local(argsOnly = true) ItemStack itemStack) {
        original.call(instance);

        if (!Animations.getInstance().isEnabled() || !Animations.getInstance().getRescale().getValue() || itemStack.isEmpty() || itemStack.has(DataComponents.MAP_ID)) return;

        float xOffset = Animations.getInstance().getX().getValue();
        float yOffset = Animations.getInstance().getY().getValue();
        float zOffset = Animations.getInstance().getZ().getValue();

        instance.translate(hand == InteractionHand.MAIN_HAND ? xOffset : -xOffset, yOffset, zOffset);

        instance.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(Animations.getInstance().getPitch().getValue())));
        instance.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(Animations.getInstance().getYaw().getValue())));
        instance.mulPose(new Matrix4f().rotation(Axis.ZP.rotationDegrees(Animations.getInstance().getRoll().getValue())));
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void handleCustomSwingAnimation(float attack, PoseStack poseStack, int i, HumanoidArm arm, CallbackInfo ci) {
        if (!Animations.getInstance().getNoSwing().getValue() || !Animations.getInstance().isEnabled()) return;
        ci.cancel();

        this.applyItemArmAttackTransform(poseStack, arm, attack);
    }
}