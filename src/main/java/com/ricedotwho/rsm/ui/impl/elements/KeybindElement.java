package com.ricedotwho.rsm.ui.impl.elements;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import com.ricedotwho.rsm.ui.impl.popups.impl.KeybindListenerPopup;
import lombok.val;
import org.jspecify.annotations.Nullable;

public class KeybindElement extends ClickHandler {
    private final TextNode textNode;
    private final Keybind keybind;
    @Nullable private final Runnable onEdit;

    public KeybindElement(Keybind keybind, @Nullable Runnable onEdit) {
        val node = new RectangleNode.Builder()
                .color(Palette.createColorContainer())
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignContent(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.CENTER)
                .height(Palette.largeElementHeight)
                .paddingLeft(12f)
                .paddingRight(12f)
                .build();

        super(node, true, false);

        this.keybind = keybind;
        this.textNode = new TextNode.Builder()
                .text(keybind.getDisplay())
                .fontSize(Palette.fontSize)
                .align(TextAlignment.CenterMiddle)
                .font(Palette.font)
                .color(Palette.createColorContainer())
                .heightPercent(100f)
                .build();
        this.addChild(textNode);
        this.onEdit = onEdit;
    }

    boolean listening = false;

    @Override
    protected void onRender(boolean hovered) {
        val accentColor = Palette.elementBackgroundLight.darker(getClickedAnimationContribution());
        val textColor = Palette.text.darker(getClickedAnimationContribution());

        node.getColor().setToColor(accentColor);
        textNode.getColor().setToColor(textColor);

        if (listening) {
            textNode.setText("...");
            return;
        }
        textNode.setText(keybind.getDisplay());
    }

    @Override
    protected void onLeftTriggered() {
        listening = true;
        KeybindListenerPopup.setListener(this::keyPressed, this::mouseClicked, this::onUnlisten);
    }

    private void onUnlisten() {
        listening = false;
        if (onEdit != null) onEdit.run();
    }

    private void mouseClicked(int button) {
        keybind.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
    }

    private void keyPressed(int keyCode) {
        InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);

        if (key.getValue() == 0 || key.getValue() == InputConstants.KEY_ESCAPE) {
            keybind.setKey(InputConstants.UNKNOWN);
            return;
        }
        keybind.setKey(key);
    }
}
