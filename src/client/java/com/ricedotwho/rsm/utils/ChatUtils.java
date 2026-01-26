package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.RSM;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

@UtilityClass
public class ChatUtils implements Accessor {

    public void chat(Object message, final Object... objects) {
        if (mc.thePlayer != null) {
            chatClean(RSM.getPrefix().getUnformattedTextForChat() + String.format(message.toString(), objects));
        }
    }

    public void chatClean(Object message, final Object... objects) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(Component.literal(String.format(message.toString())), objects);
        }
    }

    public void commandAny(String command) {
        if (command.startsWith("/")) command = command.substring(1);
        if (ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/" + command) == 0) {
            mc.thePlayer.sendChatMessage("/" + command);
        }
    }
}
