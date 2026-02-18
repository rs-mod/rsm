package com.ricedotwho.rsm.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Rotation {
    private float pitch;
    private float yaw;

    public Rotation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Rotation(Rotation other) {
        this.pitch = other.getPitch();
        this.yaw = other.getYaw();
    }

    public float getValue() {
        return Math.abs(this.yaw) + Math.abs(this.pitch);
    }

    @Override
    public String toString() {
        return "Rotation{pitch=" + pitch +
                ", yaw=" + yaw + "}";
    }

    public boolean equals(Rotation other) {
        return this.pitch == other.getPitch() && this.yaw == other.getYaw();
    }

    public float distance() {
        return this.pitchSq() + this.yawSq();
    }

    public float yawSq() {
        return this.yaw * this.yaw;
    }

    public float pitchSq() {
        return this.pitch * this.pitch;
    }
}