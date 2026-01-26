package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.Component;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KeyBindComponent extends Component {
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

    public static void register(int key, Runnable run, boolean allowGui) {
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
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen == null) return;
        checkKeybinds(true);
    }

    private void checkKeybinds(boolean gui) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        new ArrayList<>(keyBinds).stream()
                .filter(k -> k.getKeyBind() != Keyboard.KEY_NONE) // ignore unbound binds
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
