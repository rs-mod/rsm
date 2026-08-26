package com.ricedotwho.rsm.ui.impl.popups.impl.dropdown;

import com.ricedotwho.rsm.ui.api.Gui;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.UiElement;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.popups.Popup;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class DropDownPopup extends Popup {

    @Getter
    private static final DropDownPopup instance = new DropDownPopup();

    static {
        Gui.registerPopup(instance);
    }

    @Nullable
    private Runnable closedListener = null;

    public DropDownPopup() {
        val node = new RectangleNode.Builder()
                .width(Palette.largeElementWidth)
                .gap(0)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .justifyContent(JustifyContent.FLEX_START)
                .alignItems(Align.FLEX_START)
                .build();
        super(node);
        setVisible(false);
    }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isVisible()) return;
        super.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);
        MouseUtils.lockCursorRequest();
    }

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return true;
        closeMenu();
        return true;
    }

    public static void closeMenu() {
        instance.setVisible(false);
        if (instance.closedListener != null) instance.closedListener.run();
        instance.closedListener = null;
    }

    @Override
    public void onGuiClose() {
        closeMenu();
    }

    @Override
    protected boolean mouseScrolled(float verticalAmount, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        return true;
    }

    public static void open(float x, float y, float scrollY, ArrayList<? extends DropDownOption> options, @Nullable Runnable closedListener) {
        instance.clearChildren();
        closeMenu();
        instance.setLeft(x);
        instance.setTop(y + scrollY);
        for (UiElement option : options) {
            instance.addChild(option);
        }
        instance.setVisible(true);
        instance.closedListener = closedListener;
    }
}
