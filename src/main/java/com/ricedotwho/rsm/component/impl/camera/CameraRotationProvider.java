package com.ricedotwho.rsm.component.impl.camera;

import net.minecraft.world.phys.Vec3;

public interface CameraRotationProvider extends CameraProvider {
    @Override
    default boolean shouldOverridePosition() {
        return false;
    }

    @Override
    default boolean shouldOverrideHitPos() {
        return false;
    }

    @Override
    default boolean shouldOverrideHitRot() {
        return false;
    }

    @Override
    default Vec3 getCameraPosition() {
        return Vec3.ZERO;
    }

    @Override
    default Vec3 getPosForHit() {
        return Vec3.ZERO;
    }

    @Override
    default Vec3 getRotForHit() {
        return Vec3.ZERO;
    }

    @Override
    default boolean shouldBlockKeyboardMovement() {
        return false;
    }
}
