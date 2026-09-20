package com.ricedotwho.rsm.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.module.impl.player.NoCursorReset;
import com.ricedotwho.rsm.type.Accessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.SmoothDouble;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler implements Accessor {

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

    @Shadow
    public double xpos;
    @Shadow
    public double ypos;

    @Unique
    private double beforeX;
    @Unique
    private double beforeY;

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

    @Inject(method = "grabMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;xpos:D", ordinal = 0, opcode = Opcodes.PUTFIELD))
    private void onGrabMouse(CallbackInfo ci) {
        this.beforeX = this.xpos;
        this.beforeY = this.ypos;
    }

    @Inject(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;"))
    private void onReleaseMouse(CallbackInfo ci) {
        if (mc.screen instanceof ContainerScreen && NoCursorReset.shouldNotReset()) {
            InputConstants.grabOrReleaseMouse(mc.getWindow(), InputConstants.CURSOR_NORMAL, this.beforeX, this.beforeY);
            this.xpos = this.beforeX;
            this.ypos = this.beforeY;
        }
    }
}
