package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;

@UtilityClass
public class ChatUtils implements Accessor {

    public void chat(Object message, final Object... objects) {
        chatClean(RSM.getPrefix().copy().append(constructString(message.toString(), objects)));
    }

    public void chat(Component component) {
        chatClean(RSM.getPrefix().copy().append(component));
    }

    public void chatClean(Object message, final Object... objects) {
        if (mc.player != null) {
            mc.execute(() -> mc.gui.getChat().addClientSystemMessage(Component.literal(constructString(message.toString(), objects))));
        }
    }

    public void chatClean(Component message) {
        if (mc.player != null) {
            mc.execute(() -> mc.gui.getChat().addClientSystemMessage(message));
        }
    }

    private String constructString(String input, final Object... objects) {
        String[] array = input.split("\\{}", -1);
        if (array.length - 1 != objects.length) {
            RSM.getLogger().error("Input insertions not equal to objects.length");
            return input;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i < objects.length) {
                builder.append(objects[i]);
            }
        }
        return builder.toString();
    }

    public void dev(Object message, final Object... objects) {
        if (UniversalSettings.getDevInfo().getValue()) {
            chat(message, objects);
        }
    }
}
