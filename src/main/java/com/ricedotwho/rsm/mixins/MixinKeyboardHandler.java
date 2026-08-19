package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (handle != Minecraft.getInstance().getWindow().handle()) return;
        KeyInputEvent.State s = KeyInputEvent.State.get(action);
        if (s == null) return;
        if (switch (s) {
            case RELEASE -> new KeyInputEvent.Release(event).post();
            case PRESS -> new KeyInputEvent.Press(event).post();
            case REPEAT -> new KeyInputEvent.Repeat(event).post();
        } || new KeyInputEvent(s, event).post()) ci.cancel();
    }

}
