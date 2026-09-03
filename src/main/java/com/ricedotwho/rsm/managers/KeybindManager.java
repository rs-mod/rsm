package com.ricedotwho.rsm.managers;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.type.Keybind;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static com.ricedotwho.rsm.type.Accessor.mc;


@UtilityClass
@Register
public class KeybindManager {
    private final Map<InputConstants.Key, KeyList> KEYBINDS = new HashMap<>();

    public void register(Keybind keybind) {
        KEYBINDS.computeIfAbsent(keybind.getKey(), _ -> new KeyList()).add(keybind);
    }

    public void deregister(Keybind keybind) {
        KeyList list = KEYBINDS.get(keybind.getKey());
        if (list != null) {
            list.remove(keybind);
        }
    }

    public void register(InputConstants.Key key, BooleanSupplier run, boolean allowGui) {
        register(new Keybind(key, allowGui, run));
    }

    public boolean isRegistered(Keybind keybind) {
        KeyList list = KEYBINDS.get(keybind.getKey());
        if (list == null) return false;
        return list.containsAny(keybind);
    }

    public void update(Keybind keybind, InputConstants.Key newKey) {
        KeyList list = KEYBINDS.get(keybind.getKey());
        if (list == null) return;
        if (list.remove(keybind)) {
            KEYBINDS.computeIfAbsent(newKey, _ -> new KeyList()).add(keybind);
        }
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
        KeyList list = KEYBINDS.get(key);
        if (list == null) return false;
        return list.trigger(gui);
    }

    private class KeyList {
        private final List<Keybind> gui = new ArrayList<>();
        private final List<Keybind> nonGui = new ArrayList<>();

        private void add(Keybind keybind) {
            if (keybind.isAllowGui()) {
                this.gui.add(keybind);
            } else {
                this.nonGui.add(keybind);
            }
        }

        private boolean remove(Keybind keybind) {
            if (keybind.isAllowGui()) {
                return this.gui.remove(keybind);
            } else {
                return this.nonGui.remove(keybind);
            }
        }

        private boolean containsAny(Keybind keybind) {
            return this.gui.contains(keybind) || this.nonGui.contains(keybind);
        }

        private boolean trigger(boolean gui) {
            boolean bl = false;
            if (!gui) {
                for (Keybind key : this.nonGui) {
                    if (key.run()) bl = true;
                }
            }
            for (Keybind key : this.gui) {
                if (key.run()) bl = true;
            }
            return bl;
        }
    }
}
