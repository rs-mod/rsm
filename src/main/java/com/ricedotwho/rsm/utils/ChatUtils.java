package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;

@UtilityClass
public class ChatUtils implements Accessor {

    public void chat(Object message, final Object... objects) {
        chatClean(RSM.getPrefix().copy().append(String.format(message.toString(), objects)));
    }

    public void chat(Component component) {
        chatClean(RSM.getPrefix().copy().append(component));
    }

    public void chatClean(Object message, final Object... objects) {
        if (mc.player != null) {
            mc.execute(() -> mc.gui.getChat().addMessage(Component.literal(String.format(message.toString(), objects))));
        }
    }

    public void chatClean(Component message) {
        if (mc.player != null) {
            mc.execute(() -> mc.gui.getChat().addMessage(message));
        }
    }

    public void dev(Object message, final Object... objects) {
        if (RSM.getModule(ClickGUI.class).getDevInfo().getValue()) {
            chat(message, objects);
        }
    }
}
