package com.ricedotwho.rsm.component.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public void onKeyInput(KeyInputEvent.Press event) {
        event.setCancelled(checkKeybinds(mc.screen != null, event.getKey()));
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent.Click event) {
        if (event.isDown()) event.setCancelled(checkKeybinds(mc.screen != null, InputConstants.Type.MOUSE.getOrCreate(event.getButton())));
    }

    private boolean checkKeybinds(boolean gui, InputConstants.Key key) {
        if (mc.player == null || mc.level == null) return false;
        AtomicBoolean result = new AtomicBoolean(false);
        new ArrayList<>(keyBinds).stream()
                .filter(k -> k.getKeyBind() == key && (k.isAllowGui() || !gui))
                .forEach(k -> {
                    if (k.run()) result.set(true);
                });
        return result.get();
    }
}
