package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.RSM;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;

@UtilityClass
public class ChatUtils implements Accessor {

    public void chat(Object message, final Object... objects) {
        chatClean(RSM.getPrefix().copy().append(String.format(message.toString(), objects)));
    }

    public void chatClean(Object message, final Object... objects) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(String.format(message.toString(), objects)), false);
        }
    }
    public void chatClean(Component message) {
        if (mc.player != null) {
            mc.player.displayClientMessage(message, false);
        }
    }
}
