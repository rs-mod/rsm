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
    private Vec3 pickPosition(Vec3 positionVector) {
        return CameraHandler.onGetPositionForHit(positionVector);
    }

    @Unique
    private boolean isPlayer(Object entity) {
        return entity instanceof LocalPlayer;
    }

    @ModifyExpressionValue(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getXRot()F"))
    private float spoofPitch(float original) {
        if (!ClientRotationHandler.getInstance().isActive() || !isPlayer(this)) return original;
        return ClientRotationHandler.getClientPitch();
    }

    @ModifyExpressionValue(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F"))
    private float spoofYaw(float original) {
        if (!ClientRotationHandler.getInstance().isActive() || !isPlayer(this)) return original;
        return ClientRotationHandler.getClientYaw();
    }

    @WrapOperation(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setXRot(F)V"))
    private void customPitchSet(Entity instance, float xRot, Operation<Void> original) {
        if (!ClientRotationHandler.getInstance().isActive() || !isPlayer(this)) {
            original.call(instance, xRot);
            return;
        }

        ClientRotationHandler.setClientPitch(xRot);
    }

    @WrapOperation(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setYRot(F)V"))
    private void customYawSet(Entity instance, float yRot, Operation<Void> original) {
        if (!ClientRotationHandler.getInstance().isActive() || !isPlayer(this)) {
            original.call(instance, yRot);
            return;
        }

        ClientRotationHandler.setClientYaw(yRot);
    }
}
