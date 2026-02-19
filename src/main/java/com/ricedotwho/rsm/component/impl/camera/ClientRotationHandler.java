package com.ricedotwho.rsm.component.impl.camera;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ClientRotationHandler extends ModComponent implements CameraRotationProvider {
    @Getter
    private static float clientYaw = 0f;
    @Getter
    private static float clientPitch = 0f;
    private static boolean desynced = false;
    private static final List<ClientRotationProvider> providers = new ArrayList<>();

    private static float lastRotationDeltaYaw = 0f;
    private static float forwardRemainder = 0f;
    private static float strafeRemainder = 0f;
    private static boolean allowInputs;

    public ClientRotationHandler() {
        super("ClientRotationHandler");
    }

    public static void setYaw(float yaw) {
        clientYaw = yaw;
    }

    public static void setPitch(float pitch) {
        clientPitch = pitch;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start start) {
        if (Minecraft.getInstance().player == null) return;
        providers.removeIf(p -> !p.isClientRotationActive());
        allowInputs = providers.stream().allMatch(ClientRotationProvider::allowClientKeyInputs);

        boolean bl = !providers.isEmpty();
        if (bl && !desynced) {
            clientYaw = Minecraft.getInstance().player.getYRot();
            clientPitch = Minecraft.getInstance().player.getXRot();
            CameraHandler.registerProvider(this);
        }
        desynced = bl;
    }

    @SubscribeEvent
    public void onTurnPlayer(MouseInputEvent.TurnPlayer event) {
        if (!isActive()) return; //        ChatUtils.chat("turn player!");

        handleTurnPlayer(event.getD(), event.getDx(), event.getDy(), event.getSmoothTurnX(), event.getSmoothTurnY());
        event.setCancelled(true);
    }

    public static Input adjustInputsForRotation(Input inputs) {
        if (!allowInputs) return new Input(false, false, false, false, false, false, false);
        if (!desynced || Minecraft.getInstance().player == null) return inputs;

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

    private static void handleTurnPlayer(double d, double dx, double dy, SmoothDouble smoothTurnX, SmoothDouble smoothTurnY) {
        if (Minecraft.getInstance().player == null) return;
        Options options = Minecraft.getInstance().options;
        double e = options.sensitivity().get() * 0.6F + 0.2F;
        double f = e * e * e;
        double g = f * 8.0;
        double j;
        double k;
        if (options.smoothCamera) {
            double h = smoothTurnX.getNewDeltaValue(dx * g, d * g);
            double i = smoothTurnY.getNewDeltaValue(dy * g, d * g);
            j = h;
            k = i;
        } else if (options.getCameraType().isFirstPerson() && Minecraft.getInstance().player.isScoping()) {
            smoothTurnX.reset();
            smoothTurnY.reset();
            j = dx * f;
            k = dy * f;
        } else {
            smoothTurnX.reset();
            smoothTurnY.reset();
            j = dx * g;
            k = dy * g;
        }

        turn(options.invertMouseX().get() ? -j : j,options.invertMouseY().get() ? -k : k);
    }

    private static void turn(double d, double e) {
        float f = (float)e * 0.15F;
        float g = (float)d * 0.15F;
        setPitch(getClientPitch() + f);
        setYaw(getClientYaw() + g);
        setPitch(Mth.clamp(getClientPitch(), -90.0F, 90.0F));


        //Math.clamp(f % 360.0F, -90.0F, 90.0F)
    }

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
    public boolean shouldOverrideHitPos() {
        return false;
    }

    @Override
    public boolean shouldOverrideHitRot() {
        return false;
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

    @Override
    public Vec3 getPosForHit() {
        return null;
    }

    @Override
    public Vec3 getRotForHit() {
        return null;
    }
}
