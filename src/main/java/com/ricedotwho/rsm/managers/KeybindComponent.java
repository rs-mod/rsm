package com.ricedotwho.rsm.managers;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.type.Keybind;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static com.ricedotwho.rsm.type.Accessor.mc;


@UtilityClass
@Register
public class KeybindComponent {
    private final List<Keybind> keyBinds = new CopyOnWriteArrayList<>();

    public void register(Keybind keybind) {
        if (!keyBinds.contains(keybind)) keyBinds.add(keybind);
    }

    public void deregister(Keybind keybind) {
        keyBinds.remove(keybind);
    }

    public void register(InputConstants.Key key, BooleanSupplier run, boolean allowGui) {
        keyBinds.add(new Keybind(key, allowGui, run));
    }

    @SubscribeEvent
    private void onKeyInput(KeyInputEvent.Press event) {
        event.setCancelled(checkKeybinds(mc.screen != null, event.getKey()));
    }

    @SubscribeEvent
    private void onMouseInput(MouseInputEvent.Click event) {
        if (event.isDown()) event.setCancelled(checkKeybinds(mc.screen != null, InputConstants.Type.MOUSE.getOrCreate(event.getButton())));
    }

    private boolean checkKeybinds(boolean gui, InputConstants.Key key) {
        if (mc.player == null || mc.level == null || key.equals(InputConstants.UNKNOWN)) return false;
        AtomicBoolean result = new AtomicBoolean(false);
        new ArrayList<>(keyBinds).stream()
                .filter(k -> k.getKeyBind() == key && (k.isAllowGui() || !gui))
                .forEach(k -> {
                    if (k.run()) result.set(true);
                });
        return result.get();
    }
}
