package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.module.impl.player.NoBreakReset;
import com.ricedotwho.rsm.module.impl.render.Animations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandsRenderer {

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Shadow
    protected abstract void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue);

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot(F)F"))
    private float spoofPitch(LocalPlayer instance, float f) {
        return Mth.lerp(f, CameraHandler.lastPitch, CameraHandler.getPitch(instance.getXRot()));
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    private float spoofedViewPitch(LocalPlayer instance, float a) {
        //for some reason in getViewRot the subtick doesn't matter
        return CameraHandler.getPitch(instance.getViewXRot(a));
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
    private float spoofedViewYaw(LocalPlayer instance, float a) {
        //for some reason in getViewRot the subtick doesn't matter
        return CameraHandler.getYaw(instance.getViewYRot(a));
    }

    // Animations garbage, ty odin

    @WrapOperation(
            method = "renderArmWithItem",
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

        instance.mulPose(Axis.XP.rotationDegrees(Animations.getInstance().getPitch().getValue()));
        instance.mulPose(Axis.YP.rotationDegrees(Animations.getInstance().getYaw().getValue()));
        instance.mulPose(Axis.ZP.rotationDegrees(Animations.getInstance().getRoll().getValue()));
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void handleCustomSwingAnimation(float attack, PoseStack poseStack, int i, HumanoidArm arm, CallbackInfo ci) {
        if (!Animations.getInstance().getNoSwing().getValue() || !Animations.getInstance().isEnabled()) return;
        ci.cancel();

        this.applyItemArmAttackTransform(poseStack, arm, attack);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
            )
    )
    private void applySizeTransform(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getRescale().getValue() && !itemStack.isEmpty() && !itemStack.has(DataComponents.MAP_ID)) poseStack.scale(Animations.getInstance().getScale().getValue(), Animations.getInstance().getScale().getValue(), Animations.getInstance().getScale().getValue());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void maintainCustomEquipHeights(CallbackInfo ci) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoEquip().getValue()) {
            this.oMainHandHeight = 1.0f;
            this.mainHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
            this.offHandHeight = 1.0f;
        }
    }

    @Inject(at = @At("RETURN"), method = "shouldInstantlyReplaceVisibleItem", cancellable = true)
    private void onShouldInstantlyReplaceVisibleItem(ItemStack currentlyVisibleItem, ItemStack expectedItem, CallbackInfoReturnable<Boolean> cir) {
        if (Animations.getInstance().isEnabled() && Animations.getInstance().getNoEquip().getValue()) {
            cir.setReturnValue(true);
            return;
        }
        if (!cir.getReturnValue() && NoBreakReset.getInstance().isEnabled() && NoBreakReset.getInstance().getStopFlicker().getValue()) {
            cir.setReturnValue(NoBreakReset.compare(currentlyVisibleItem, expectedItem));
        }
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"))
    private float overrideAttackStrengthScale(float originalValue) {
        if (Animations.getInstance().isEnabled() && (Animations.getInstance().getNoEquip().getValue() || Animations.getInstance().getNoSwing().getValue())) return 1f;
        return originalValue;
    }
}
