package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.RotationState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements Accessor {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void preRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        RotationState.push(player);

        if (CameraHandler.hasYaw()) {
            float yaw = CameraHandler.getYaw(player.getYRot());
            player.setYRot(yaw);
            player.yRotO = yaw;
        }

        if (CameraHandler.hasPitch()) {
            float pitch = CameraHandler.getPitch(player.getXRot());
            player.setXRot(pitch);
            player.xRotO = pitch;
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void postRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        RotationState.pop();
    }
}
