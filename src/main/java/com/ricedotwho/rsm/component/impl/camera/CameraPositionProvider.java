package com.ricedotwho.rsm.component.impl.camera;

public interface CameraPositionProvider extends CameraProvider {
    @Override
    default boolean shouldOverrideYaw() {
        return false;
    }

    @Override
    default boolean shouldOverridePitch() {
        return false;
    }

    @Override
    default boolean shouldBlockMouseMovement() {
        return false;
    }

    @Override
    default float getYaw() {
        return 0f;
    }
    @Override
    default float getPitch() {
        return 0f;
    }
}
