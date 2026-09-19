package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.module.impl.player.Chat;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ChatComponent.class, priority = 300) // Low priority, will apply before others, so can be overridden if something tries to set it higher?
public class MixinChatComponent {
    @Shadow
    @Final
    public List<GuiMessage.Line> trimmedMessages;

    @ModifyConstant(
            method = "addRecentChat",
            constant = @Constant(intValue = 100)
    )
    private int modifyMaxMessages(int original) {
        return 10000;
    }

    @WrapOperation(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void onAddMessage(ChatComponent instance, int dir, Operation<Void> original) {
        if (Chat.getInstance().getStopAutoScroll().getValue()) return;
        original.call(instance, dir);
    }

    @Inject(method = "clearMessages", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;flushQueue()V", shift = At.Shift.AFTER), cancellable = true)
    private void onClearMessages(boolean history, CallbackInfo ci) {
        if (!Chat.getInstance().isEnabled() || !Chat.getInstance().getDontClearHistory().getValue()) return;
        ci.cancel();
    }

    @Inject(
            method = "addMessageToDisplayQueue",
            at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift = At.Shift.AFTER)
    )
    private void onAddMessage(GuiMessage message, CallbackInfo ci) {
        Chat.getLineCache().put(this.trimmedMessages.getFirst(), message);
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("HEAD"))
    private void onRefreshTrimmedMessages(CallbackInfo ci) {
        Chat.getLineCache().clear();
    }
}
