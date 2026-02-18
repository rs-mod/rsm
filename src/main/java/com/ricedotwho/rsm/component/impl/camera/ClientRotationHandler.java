package com.ricedotwho.rsm.component.impl.camera;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;

import java.util.ArrayList;
import java.util.List;

public class ClientRotationHandler extends ModComponent implements CameraRotationProvider {
    @Getter
    private static float clientYaw = 0f;
    @Getter
    private static float clientPitch = 0f;
    private static boolean desynced = false;
    private static final List<ClientRotationProvider> providers = new ArrayList<>();

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
        providers.removeIf(p -> !p.isActive());
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
}
