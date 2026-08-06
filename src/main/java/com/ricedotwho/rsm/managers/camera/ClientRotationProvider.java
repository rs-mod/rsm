package com.ricedotwho.rsm.managers.camera;

public interface ClientRotationProvider {
    boolean isClientRotationActive();
    boolean allowClientKeyInputs();

    default boolean isDesyncPaused() {
        return false;
    }

    default void onDesyncDisable() {}
    default void onDesyncPause() {}
}
