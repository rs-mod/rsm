package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.module.impl.render.Freecam;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", ordinal = 0))
    private boolean onRenderLevel(CameraType instance) {
        return instance.isFirstPerson() && !Freecam.isDetached();
    }

    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 0)
    private Vec3 pickPosition(Vec3 positionVector) {
        return CameraHandler.onGetPositionForHit(positionVector);
    }

    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 1)
    private Vec3 pickRotation(Vec3 rotationVector) {
        return CameraHandler.onGetRotationForHit(rotationVector);
    }
}
