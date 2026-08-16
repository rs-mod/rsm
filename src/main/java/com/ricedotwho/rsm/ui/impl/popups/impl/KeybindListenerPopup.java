package com.ricedotwho.rsm.ui.impl.popups.impl;

import com.ricedotwho.rsm.ui.api.Gui;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.popups.Popup;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * This is to give keybinds higher mouse priority.
 * This is used so the event is cancellable
 */
public class KeybindListenerPopup extends Popup {
    @Getter
    private static final KeybindListenerPopup instance = new KeybindListenerPopup();
    static {
        Gui.registerPopup(instance);
    }

    private boolean listening = false;
    @Nullable private Consumer<Integer> keyPressed = null;
    @Nullable private Consumer<Integer> mouseClicked = null;
    private Runnable onUnlisten = null;

    public KeybindListenerPopup() {
        super(new RectangleNode.Builder().build());
    }

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!listening) return false;
        listening = false;
        if (mouseClicked != null) mouseClicked.accept(button);
        onUnlisten.run();
        return true;
    }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isVisible()) return;
        MouseUtils.lockCursorRequest();
    }

    @Override
    protected boolean mouseScrolled(float verticalAmount, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        return listening;
    }

    @Override
    protected boolean keyPressed(int keyCode, float mouseX, float mouseY, float scrollY) {
        if (!listening) return false;
        listening = false;
        if (keyPressed != null) keyPressed.accept(keyCode);
        onUnlisten.run();
        return true;
    }

    public static void setListener(@Nullable Consumer<Integer> keyPressed, @Nullable Consumer<Integer> mouseClicked, Runnable onUnlisten) {
        instance.keyPressed = keyPressed;
        instance.mouseClicked = mouseClicked;
        instance.onUnlisten = onUnlisten;
        instance.listening = true;
    }
}
