package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Shadow
    @Final
    private SmoothDouble smoothTurnX;

    @Shadow
    @Final
    private SmoothDouble smoothTurnY;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (handle != Minecraft.getInstance().getWindow().handle()) return;
        if (new MouseInputEvent.Click(action == 1, rawButtonInfo.button(), rawButtonInfo.modifiers()).post()) ci.cancel();
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (handle != Minecraft.getInstance().getWindow().handle()) return;
        if (new MouseInputEvent.Scroll(yoffset).post()) ci.cancel();
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void onMove(long handle, double xpos, double ypos, CallbackInfo ci) {
        if (handle != Minecraft.getInstance().getWindow().handle()) return;
        if (new MouseInputEvent.Move(xpos, ypos).post()) ci.cancel();
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onTurnPlayer(double mousea, CallbackInfo ci) {
        if (new MouseInputEvent.TurnPlayer(mousea, this.accumulatedDX, this.accumulatedDY, this.smoothTurnX, this.smoothTurnY).post()) ci.cancel();
    }
}
