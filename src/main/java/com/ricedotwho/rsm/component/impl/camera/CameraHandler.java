package com.ricedotwho.rsm.component.impl.camera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.CameraSetupEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CameraHandler extends ModComponent {
    private static final byte YAW_FLAG = 0x01;
    private static final byte PITCH_FLAG = 0x02;
    private static final byte POSITION_FLAG = 0x04;
    private static final byte BLOCK_KEYS_FLAG = 0x08;
    private static final byte BLOCK_MOUSE_FLAG = 0x10;
    private static final byte HIT_ROT_FLAG = 0x20;
    private static final byte HIT_POS_FLAG = 0x40;

    private static final List<CameraProvider> providers = new ArrayList<>();

    private static float yaw = 0.0f;
    private static float pitch = 0.0f;
    public static float lastYaw = 0.0f;
    public static float lastPitch = 0.0f;
    @Getter
    private static Vec3 cameraPos = Vec3.ZERO;
    private static Vec3 hitPos = Vec3.ZERO;
    private static Vec3 hitRot = Vec3.ZERO;
    private static final BlockPos.MutableBlockPos cameraBlockPos = BlockPos.MutableBlockPos.ZERO.mutable();
    private static byte flags = 0;

    public CameraHandler() {
        super("Camera Handler");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender(CameraSetupEvent event) {
        flags = 0;
        if (providers.isEmpty()) return;
        providers.removeIf(p -> !p.isActive());

        if (providers.isEmpty()) return;
        if (providers.stream().anyMatch(CameraProvider::shouldBlockKeyboardMovement)) flags |= BLOCK_KEYS_FLAG;
        if (providers.stream().anyMatch(CameraProvider::shouldBlockMouseMovement)) flags |= BLOCK_MOUSE_FLAG;

        // This allows for getPriority adjust dynamically, it could be sorted when adding them if the priorities are constant
        // I don't foresee this being an issue as they will rarely contain more than provider at a time
        List<CameraProvider> sortedProviders = providers.stream().sorted(Comparator.comparingInt(CameraProvider::getPriority)).toList();

        CameraProvider positionProvider = sortedProviders.stream().filter(CameraProvider::shouldOverridePosition).findFirst().orElse(null);
        CameraProvider yawProvider = sortedProviders.stream().filter(CameraProvider::shouldOverrideYaw).findFirst().orElse(null);
        CameraProvider pitchProvider = sortedProviders.stream().filter(CameraProvider::shouldOverridePitch).findFirst().orElse(null);
        CameraProvider hitPosProvider = sortedProviders.stream().filter(CameraProvider::shouldOverrideHitPos).findFirst().orElse(null);
        CameraProvider hitRotProvider = sortedProviders.stream().filter(CameraProvider::shouldOverrideHitRot).findFirst().orElse(null);

        if (positionProvider != null) {
            cameraPos = positionProvider.getCameraPosition();
            cameraBlockPos.set(cameraPos.x, cameraPos.y, cameraPos.z);
            flags |= POSITION_FLAG;
        }

        if (yawProvider != null) {
            yaw = yawProvider.getYaw();
            flags |= YAW_FLAG;
        }

        if (pitchProvider != null) {
            pitch = pitchProvider.getPitch();
            flags |= PITCH_FLAG;
        }

        if (hitPosProvider != null) {
            hitPos = hitPosProvider.getPosForHit();
            flags |= HIT_POS_FLAG;
        }

        if (hitRotProvider != null) {
            hitRot = hitRotProvider.getRotForHit();
            flags |= HIT_ROT_FLAG;
        }
    }

    @SubscribeEvent
    public void onTickEnd(ClientTickEvent.End event) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        lastYaw = hasYaw() ? yaw : player.getYRot();
        lastPitch = hasPitch() ? pitch : player.getXRot();
    }

    public static void registerProvider(CameraProvider cameraProvider) {
        providers.add(cameraProvider);
    }

    public static void onGetCameraPos(CallbackInfoReturnable<Vec3> cir) {
        if ((flags & POSITION_FLAG) == 0 || cameraPos == null) return;
        cir.setReturnValue(cameraPos);
    }

    public static void onGetCameraRotation(Camera instance, float yRot, float xRot, Operation<Void> original) {
        original.call(instance,
                (flags & YAW_FLAG) == 0 ? yRot : yaw,
                (flags & PITCH_FLAG) == 0 ? xRot : pitch
        );
    }

    public static Input onPrePollInputs(Input inputs) {
        if ((flags & BLOCK_KEYS_FLAG) == 0) return inputs;
        return new Input(false, false, false, false, false, false, false);
    }

    public static Vec3 onGetPositionForHit(Vec3 vec) {
        if ((flags & HIT_POS_FLAG) == 0) return vec;
        return hitPos;
    }

    public static Vec3 onGetRotationForHit(Vec3 vec) {
        if ((flags & HIT_ROT_FLAG) == 0) return vec;
        return hitRot;
    }

    public static boolean hasAnyRotation() {
        return (flags & (PITCH_FLAG | YAW_FLAG)) != 0;
    }

    public static float getPitch(float original) {
        if ((flags & PITCH_FLAG) == 0) return original;
        return pitch;
    }

    public static float getYaw(float original) {
        if ((flags & YAW_FLAG) == 0) return original;
        return yaw;
    }

    public static Vec3 getPos(Vec3 original) {
        if ((flags & POSITION_FLAG) == 0) return original;
        return cameraPos;
    }

    public static boolean hasYaw() {
        return (flags & YAW_FLAG) != 0;
    }

    public static boolean hasPitch() {
        return (flags & PITCH_FLAG) != 0;
    }

    @SubscribeEvent
    public static void onTurnPlayer(MouseInputEvent.TurnPlayer event) {
        if ((flags & BLOCK_MOUSE_FLAG) == 0) return;
        event.setCancelled(true);
    }
}
