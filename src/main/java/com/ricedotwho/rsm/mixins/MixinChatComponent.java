package com.ricedotwho.rsm.mixins;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ChatComponent.class, priority = 300) // Low priority, will apply before others, so can be overriden if something tries to set it higher?
public class MixinChatComponent {
    @ModifyConstant(
            method = "addRecentChat",
            constant = @Constant(intValue = 100)
    )
    private int modifyMaxMessages(int original) {
        return 10000;
    }
}
