package com.ricedotwho.rsm.component.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KeybindComponent extends ModComponent {
    private static final List<Keybind> keyBinds = new CopyOnWriteArrayList<>();

    public KeybindComponent() {
        super("KeyBindComponent");
    }

    public static void register(Keybind keybind) {
        if(!keyBinds.contains(keybind)) keyBinds.add(keybind);
    }

    public static void deregister(Keybind keybind) {
        keyBinds.remove(keybind);
    }

    public static void register(InputConstants.Key key, Runnable run, boolean allowGui) {
        keyBinds.add(new Keybind(key, allowGui, run));
    }

    @SubscribeEvent
    public void keyEvent(InputEvent event) {
        checkKeybinds(mc.screen != null, event.getKey());
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Start event) {
        if (mc.player == null || mc.level == null || mc.screen == null) return;
        checkKeybinds(true, null);
    }

    private void checkKeybinds(boolean gui, InputConstants.Key key) {
        if (mc.player == null || mc.level == null) return;
        new ArrayList<>(keyBinds).stream()
                .filter(k -> k.getKeyBind().getValue() != GLFW.GLFW_KEY_UNKNOWN) // ignore unbound binds
                .forEach(k -> {
                    if(k.isAllowGui() || !gui) {
                        boolean pressed = key == null ? k.isActive() : k.getKeyBind() == key;
                        if (pressed && !k.wasPressed()) {
                            k.run();
                        }
                        if (pressed) {
                            k.onPress();
                        }
                    }
                });
    }
}
