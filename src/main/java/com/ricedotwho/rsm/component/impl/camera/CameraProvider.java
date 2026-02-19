package com.ricedotwho.rsm.component.impl.camera;

import net.minecraft.world.phys.Vec3;

public interface CameraProvider {
    default int getPriority() {
        return 100;
    }

    boolean shouldOverridePosition();
    boolean shouldOverrideYaw();
    boolean shouldOverridePitch();

    boolean shouldOverrideHitPos();
    boolean shouldOverrideHitRot();

    boolean shouldBlockKeyboardMovement();
    boolean shouldBlockMouseMovement();

    default boolean isActive() {
        return shouldOverridePosition() || shouldOverrideYaw() || shouldOverridePitch();
    }

    Vec3 getCameraPosition();
    float getYaw();
    float getPitch();
    Vec3 getPosForHit();
    Vec3 getRotForHit();
}
