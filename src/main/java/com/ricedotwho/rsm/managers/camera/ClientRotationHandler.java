package com.ricedotwho.rsm.managers.camera;

import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.render.CameraSetupEvent;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@Register
public class ClientRotationHandler implements CameraRotationProvider {
    @SuppressWarnings({"unused"})
    @Getter
    private static final ClientRotationHandler instance = new ClientRotationHandler();

    @Getter
    @Setter
    private static float clientYaw = Float.NaN;
    @Getter
    @Setter
    private static float clientPitch = Float.NaN;
    private static boolean desynced = false;
    private static final List<ClientRotationProvider> providers = new ArrayList<>();

    private static float lastRotationDeltaYaw = 0f;
    private static float forwardRemainder = 0f;
    private static float strafeRemainder = 0f;
    private static boolean allowInputs;

    private static boolean lastPausedState = false;

    public static void setYaw(float yaw) {
        clientYaw = yaw;
    }

    public static void setPitch(float pitch) {
        clientPitch = pitch;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onFrame(CameraSetupEvent start) {
        if (Minecraft.getInstance().player == null) return;
        providers.removeIf(p -> !p.isClientRotationActive() && invoke(p::onDesyncDisable));
        allowInputs = providers.stream().allMatch(ClientRotationProvider::allowClientKeyInputs);

        boolean bl = providers.stream().anyMatch(provider -> !provider.isDesyncPaused());

        if (!providers.isEmpty()) {
            if (!(bl || lastPausedState)) {
                providers.forEach(ClientRotationProvider::onDesyncPause);
            }
            lastPausedState = !bl;
        } else {
            lastPausedState = false;
        }

        if (bl && !desynced) {
            // On enable
            if (Float.isNaN(clientYaw))
                clientYaw = Minecraft.getInstance().player.getYRot();
            if (Float.isNaN(clientPitch))
                clientPitch = Minecraft.getInstance().player.getXRot();
            CameraHandler.registerProvider(this);
        }
        if (!bl && desynced) {
            // On disable
            Minecraft.getInstance().player.yRotO = clientYaw;
            Minecraft.getInstance().player.xRotO = clientPitch;
            clientYaw = Float.NaN;
            clientPitch = Float.NaN;
        }
        desynced = bl;
    }

    private boolean invoke(Runnable runnable) {
        runnable.run();
        return true;
    }

    public static Input adjustInputsForRotation(Input inputs) {
        if (!allowInputs) return new Input(false, false, false, false, false, false, false);
        if (!desynced || Minecraft.getInstance().player == null) return inputs;
        if (Float.isNaN(clientYaw)) return inputs;

        Vec2 moveVector = RotationUtils.constructMovementVector(inputs);
        if (moveVector.x == 0f && moveVector.y == 0f) {
            forwardRemainder = 0f;
            strafeRemainder = 0f;
            lastRotationDeltaYaw = clientYaw - Minecraft.getInstance().player.getYRot();
            return inputs;
        }


        float currentDeltaYaw = clientYaw - Minecraft.getInstance().player.getYRot();
        float deltaYaw = currentDeltaYaw - lastRotationDeltaYaw;
        if (deltaYaw != 0f) {
            // Rotate the remainders to the new yaw
            Vec2 newRemainder = RotationUtils.rotateVector(forwardRemainder, strafeRemainder, deltaYaw);
            forwardRemainder = newRemainder.x;
            strafeRemainder = newRemainder.y;
        }

        lastRotationDeltaYaw = currentDeltaYaw;
        Vec2 rotatedMovementVector = RotationUtils.rotateVector(moveVector.x, moveVector.y, currentDeltaYaw);
        float newForward = Mth.clamp(rotatedMovementVector.x - forwardRemainder, -1f, 1f);
        float newStrafe = Mth.clamp(rotatedMovementVector.y - strafeRemainder, -1f, 1f);

        float forwardsMovement = Math.round(newForward);
        float strafeMovement = Math.round(newStrafe);

        forwardRemainder = forwardsMovement - newForward;
        strafeRemainder = strafeMovement - newStrafe;
        return getInputsFromVec(forwardsMovement, strafeMovement, inputs);
    }

    private static Input getInputsFromVec(float forwards, float strafe, Input inputs) {
        return new Input(forwards == 1f, forwards == -1f, strafe == 1f, strafe == -1f, inputs.jump(), inputs.shift(), inputs.sprint());
    }

    @SuppressWarnings({"unused"})
    public static void registerProvider(ClientRotationProvider provider) {
        providers.add(provider);
    }


    @Override
    public boolean shouldOverrideYaw() {
        return desynced;
    }

    @Override
    public boolean shouldOverridePitch() {
        return desynced;
    }

    @Override
    public boolean shouldBlockMouseMovement() {
        return false; // Will cancel anyways but whatever
    }

    @Override
    public float getYaw() {
        return clientYaw;
    }

    @Override
    public float getPitch() {
        return clientPitch;
    }

    @SuppressWarnings({"unused"})
    public static void syncServerRotationToClient() {
        if (Minecraft.getInstance().player == null) return;
        if (Float.isNaN(clientYaw) || Float.isNaN(clientPitch)) return;
        Minecraft.getInstance().player.setYRot(clientYaw);
        Minecraft.getInstance().player.setXRot(clientPitch);
    }

    @Override
    public Vec3 getPosForHit() {
        return null;
    }

    @Override
    public Vec3 getRotForHit() {
        return null;
    }
}
