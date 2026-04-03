package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.data.MutableInput;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/KeyboardInput;keyPresses:Lnet/minecraft/world/entity/player/Input;", opcode = Opcodes.PUTFIELD))
    private void onTick(KeyboardInput instance, Input value) {
        // Don't forget to set it if you remove the onPrePollInputs
        // instance.keyPresses is null here
        instance.keyPresses = ClientRotationHandler.adjustInputsForRotation(CameraHandler.onPrePollInputs(value));
        InputPollEvent e = new InputPollEvent(instance.keyPresses, new MutableInput(instance.keyPresses), (Minecraft.getInstance().player != null && instance == Minecraft.getInstance().player.input));
        e.post();
        instance.keyPresses = e.getInput().toInput();
    }
}
