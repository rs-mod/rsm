package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ricedotwho.rsm.module.impl.player.Chat;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public class MixinChatComponentLineConsumer {

    @WrapOperation(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z"))
    private boolean onAcceptLine(ChatComponent.ChatGraphicsAccess instance, int i, float v, FormattedCharSequence formattedCharSequence, Operation<Boolean> original, @Local(argsOnly = true) GuiMessage.Line line) {
        boolean hovered = original.call(instance, i, v, formattedCharSequence);
        if (hovered) Chat.setLastHovered(line);
        return hovered;
    }
}
