package com.ricedotwho.rsm.type;

import com.google.gson.JsonPrimitive;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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

    public float getXRot() {
        return pitch;
    }

    public float getYRot() {
        return yaw;
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

    public static Rotation from(Vec3 to, Vec3 from) {
        return RotationUtils.getRotation(from, to);
    }

    public static Rotation from(Vec3 to) {
        Player player = Minecraft.getInstance().player;
        Vec3 from = player.position().add(0, player.getEyeHeight(player.getPose()), 0);
        return RotationUtils.getRotation(from, to);
    }

    public JsonPrimitive getAsJsonPrimitive() {
        return new JsonPrimitive(this.getYaw() + " " + this.getPitch());
    }

    public static Rotation fromJsonPrimitive(JsonPrimitive primitive) throws IllegalArgumentException {
        String[] parts = primitive.getAsString().trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Rotation format: \"" + primitive.getAsString() + "\"");
        }
        float yaw = Float.parseFloat(parts[0]);
        float pitch = Float.parseFloat(parts[1]);
        return new Rotation(yaw, pitch);
    }
}