package com.ricedotwho.rsm.component.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KeyBindComponent extends ModComponent {
    private static final List<Keybind> keyBinds = new CopyOnWriteArrayList<>();

    public KeyBindComponent() {
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
    public void keyEvent(InputEvent.KeyInputEvent event) {
        checkKeybinds(false);
    }

    @SubscribeEvent
    public void mouseEvent(InputEvent.MouseInputEvent event) {
        checkKeybinds(false);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Start event) {
        if (mc.player == null || mc.level == null || mc.screen == null) return;
        checkKeybinds(true);
    }

    private void checkKeybinds(boolean gui) {
        if (mc.player == null || mc.level == null) return;
        new ArrayList<>(keyBinds).stream()
                .filter(k -> k.getKeyBind().getValue() != 0 && k.getKeyBind().getValue() != GLFW.GLFW_KEY_ESCAPE) // ignore unbound binds
                .forEach(k -> {
                    if((k.isAllowGui() || !gui)) {
                        boolean pressed = k.isActive();
                        if (pressed && !k.isPressed()) {
                            k.run();
                        }
                        k.setPressed(pressed);
                    }
                });
    }
}
