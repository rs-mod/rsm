package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.game.HitProcessEvent;
import com.ricedotwho.rsm.module.impl.render.Freecam;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", ordinal = 0))
    private boolean onRenderLevel(CameraType instance) {
        return instance.isFirstPerson() && !Freecam.isDetached();
    }

    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 0)
    private Vec3 pickPosition(Vec3 positionVector) {
        AtomicReference<Vec3> ref = new AtomicReference<>(positionVector);
        new HitProcessEvent.Position(ref::set).post();
        return ref.get();
    }

    @ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("STORE"), ordinal = 1)
    private Vec3 pickRotation(Vec3 rotationVector) {
        AtomicReference<Vec3> ref = new AtomicReference<>(rotationVector);
        new HitProcessEvent.Rotation(ref::set).post();
        return ref.get();
    }
}
