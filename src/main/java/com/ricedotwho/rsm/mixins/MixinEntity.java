package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.managers.camera.CameraHandler;
import com.ricedotwho.rsm.managers.camera.ClientRotationHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class MixinEntity {

    @ModifyVariable(method = "pick", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private Vec3 rsm$pickPosition(Vec3 from) {
        return CameraHandler.onGetPositionForHit(from);
    }

    @Unique
    private boolean rsm$isNotPlayer(Object entity) {
        return !(entity instanceof LocalPlayer);
    }

    @ModifyExpressionValue(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getXRot()F"))
    private float rsm$spoofPitch(float original) {
        if (!ClientRotationHandler.getInstance().isActive() || rsm$isNotPlayer(this)) return original;
        return ClientRotationHandler.getClientPitch();
    }

    @ModifyExpressionValue(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F"))
    private float rsm$spoofYaw(float original) {
        if (!ClientRotationHandler.getInstance().isActive() || rsm$isNotPlayer(this)) return original;
        return ClientRotationHandler.getClientYaw();
    }

    @WrapOperation(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setXRot(F)V"))
    private void rsm$customPitchSet(Entity instance, float xRot, Operation<Void> original) {
        if (!ClientRotationHandler.getInstance().isActive() || rsm$isNotPlayer(this)) {
            original.call(instance, xRot);
            return;
        }

        ClientRotationHandler.setClientPitch(xRot);
    }

    @WrapOperation(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setYRot(F)V"))
    private void rsm$customYawSet(Entity instance, float yRot, Operation<Void> original) {
        if (!ClientRotationHandler.getInstance().isActive() || rsm$isNotPlayer(this)) {
            original.call(instance, yRot);
            return;
        }

        ClientRotationHandler.setClientYaw(yRot);
    }
}
