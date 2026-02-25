package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 0)
    private Vec3 pickPosition(Vec3 positionVector) {
        return CameraHandler.onGetPositionForHit(positionVector);
    }

    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 1)
    private Vec3 pickRotation(Vec3 rotationVector) {
        return CameraHandler.onGetRotationForHit(rotationVector);
    }
}
