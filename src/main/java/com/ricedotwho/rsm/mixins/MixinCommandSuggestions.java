package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.core.UniversalSettings;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public class MixinCommandSuggestions {

    @Unique
    boolean isRSMCommand = false;

    @Inject(method = "updateCommandInfo", at = @At("HEAD"))
    public void startOfUpdateCommandInfo(CallbackInfo ci) {
        isRSMCommand = false;
    }

    @WrapOperation(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;peek()C"))
    private char wrapPeek(StringReader reader, Operation<Character> original) {
        isRSMCommand = reader.peek() == UniversalSettings.getCommandPrefix().getValue().charAt(0);
        if (isRSMCommand) {
            return '/';
        }
        return original.call(reader);
    }

    @WrapOperation(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;getCommands()Lcom/mojang/brigadier/CommandDispatcher;"))
    private CommandDispatcher<ClientSuggestionProvider> wrapGetCommands(ClientPacketListener instance, Operation<CommandDispatcher<ClientSuggestionProvider>> original) {
        if (isRSMCommand) {
            return RSM.getInstance().getCommandManager().getDispatcher();
        }
        return original.call(instance);
    }
}
