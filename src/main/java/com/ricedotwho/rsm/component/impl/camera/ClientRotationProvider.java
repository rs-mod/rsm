package com.ricedotwho.rsm.component.impl.camera;

public interface ClientRotationProvider {
    boolean isClientRotationActive();
    boolean allowClientKeyInputs();

    default boolean isDesyncPaused() {
        return false;
    }

    default void onDesyncDisable() {}
    default void onDesyncPause() {}
}
