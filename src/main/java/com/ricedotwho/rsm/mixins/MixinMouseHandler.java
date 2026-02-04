package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onButton(long window, MouseButtonInfo info, int state, CallbackInfo ci) {
        if (window != Minecraft.getInstance().getWindow().handle()) return;
        if (new MouseInputEvent.Click(state == 1, info.button(), info.modifiers()).post()) ci.cancel();
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double d, double dir, CallbackInfo ci) {
        if (window != Minecraft.getInstance().getWindow().handle()) return;
        if (new MouseInputEvent.Scroll(dir).post()) ci.cancel();
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void onMove(long window, double x, double y, CallbackInfo ci) {
        if (window != Minecraft.getInstance().getWindow().handle()) return;
        if (new MouseInputEvent.Move(x, y).post()) ci.cancel();
    }

}
