package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
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
        instance.keyPresses = value;
        new InputPollEvent(instance.keyPresses, input -> instance.keyPresses = input).post();
    }
}
