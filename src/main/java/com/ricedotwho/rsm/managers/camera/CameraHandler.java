package com.ricedotwho.rsm.managers.camera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.CameraSetupEvent;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.ricedotwho.rsm.type.Accessor.mc;

@UtilityClass
@Register
public class CameraHandler {
    private final byte YAW_FLAG = 0x01;
    private final byte PITCH_FLAG = 0x02;
    private final byte POSITION_FLAG = 0x04;
    private final byte BLOCK_KEYS_FLAG = 0x08;
    private final byte BLOCK_MOUSE_FLAG = 0x10;
    private final byte HIT_ROT_FLAG = 0x20;
    private final byte HIT_POS_FLAG = 0x40;

    private final List<CameraProvider> providers = new ArrayList<>();

    private float yaw = 0.0f;
    private float pitch = 0.0f;
    public float lastYaw = 0.0f;
    public float lastPitch = 0.0f;
    @Getter
    private Vec3 cameraPos = Vec3.ZERO;
    private Vec3 hitPos = Vec3.ZERO;
    private Vec3 hitRot = Vec3.ZERO;
    private final BlockPos.MutableBlockPos cameraBlockPos = BlockPos.MutableBlockPos.ZERO.mutable();
    private byte flags = 0;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onRender(CameraSetupEvent event) {
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
    private void onTickEnd(ClientTickEvent.End event) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        lastYaw = hasYaw() ? yaw : player.getYRot();
        lastPitch = hasPitch() ? pitch : player.getXRot();
    }

    public void registerProvider(CameraProvider cameraProvider) {
        providers.add(cameraProvider);
    }

    public void onGetCameraPos(CallbackInfoReturnable<Vec3> cir) {
        if ((flags & POSITION_FLAG) == 0 || cameraPos == null) return;
        cir.setReturnValue(cameraPos);
    }

    public void onGetCameraRotation(Camera instance, float yRot, float xRot, Operation<Void> original) {
        original.call(instance,
                (flags & YAW_FLAG) == 0 ? yRot : yaw,
                (flags & PITCH_FLAG) == 0 ? xRot : pitch
        );
    }

    public Input onPrePollInputs(Input inputs) {
        if ((flags & BLOCK_KEYS_FLAG) == 0) return inputs;
        return new Input(false, false, false, false, false, false, false);
    }

    public Vec3 onGetPositionForHit(Vec3 vec) {
        if ((flags & HIT_POS_FLAG) == 0) return vec;
        return hitPos;
    }

    public Vec3 onGetRotationForHit(Vec3 vec) {
        if ((flags & HIT_ROT_FLAG) == 0) return vec;
        return hitRot;
    }

    public boolean hasAnyRotation() {
        return (flags & (PITCH_FLAG | YAW_FLAG)) != 0;
    }

    public float getPitch(float original) {
        if ((flags & PITCH_FLAG) == 0) return original;
        return pitch;
    }

    public float getYaw(float original) {
        if ((flags & YAW_FLAG) == 0) return original;
        return yaw;
    }

    public Vec3 getPos(Vec3 original, float partialTickTime, float eyeHeightOld, float eyeHeight) {
        if ((flags & POSITION_FLAG) == 0) return original;
        return cameraPos.add(0, Mth.lerp(partialTickTime, eyeHeightOld, eyeHeight), 0);
    }

    public boolean hasYaw() {
        return (flags & YAW_FLAG) != 0;
    }

    public boolean hasPosition() {
        return (flags & POSITION_FLAG) != 0;
    }

    public boolean hasPitch() {
        return (flags & PITCH_FLAG) != 0;
    }

    @SubscribeEvent
    private void onTurnPlayer(MouseInputEvent.TurnPlayer event) {
        if ((flags & BLOCK_MOUSE_FLAG) == 0) return;
        event.setCancelled(true);
    }
}
