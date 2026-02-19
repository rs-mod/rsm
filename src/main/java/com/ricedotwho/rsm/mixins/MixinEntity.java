package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class MixinEntity {

    @ModifyVariable(method = "pick", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private Vec3 pickPosition(Vec3 positionVector) {
        return CameraHandler.onGetPositionForHit(positionVector);
    }
}
