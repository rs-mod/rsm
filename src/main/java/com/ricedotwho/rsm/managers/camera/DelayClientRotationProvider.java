package com.ricedotwho.rsm.managers.camera;

public class DelayClientRotationProvider implements ClientRotationProvider {
    private int ticks;
    private final boolean allowInputs;

    /**
     * @param ticks (decrements on tick end)
     * @param allowInputs whether to allow inputs
     */
    public DelayClientRotationProvider(int ticks, boolean allowInputs) {
        this.ticks = ticks;
        this.allowInputs = allowInputs;
    }

    public void onTickEnd() {
        ticks--;
    }

    @Override
    public boolean isClientRotationActive() {
        return ticks > 0;
    }

    @Override
    public boolean allowClientKeyInputs() {
        return allowInputs;
    }
}
