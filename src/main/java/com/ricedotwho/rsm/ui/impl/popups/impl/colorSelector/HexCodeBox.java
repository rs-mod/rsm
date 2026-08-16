package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.elements.TextBox;
import lombok.val;

class HexCodeBox extends TextBox {
    private final boolean includeAlpha;
    ColorPopup colorPopup;
    String hexCode = "";

    protected HexCodeBox(ColorPopup colorPopup, boolean includeAlpha) {
        val yogaNode = new YogaNodeBuilder()
                .flexGrow(1f)
                .padding(Palette.elementInteriorPadding)
                .build();
        super(
                null,
                null,
                "",
                null,
                "[^0-9|^A-F|^a-f]",
                yogaNode,
                TextAlignment.CenterMiddle
        );

        this.colorPopup = colorPopup;
        setOnUnlisten(this::parseText);
        setTextSupplier(() -> hexCode);
        setTextConsumer((string) -> hexCode = string);
        this.includeAlpha = includeAlpha;
    }

    @Override
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {

        val targetColor = colorPopup.getTargetColor();
        if (!isListening()) {
            hexCode = "#" + targetColor.getHexCode(includeAlpha);
            super.frame(parentX, parentY, mouseX, mouseY, scrollY);
            return;
        }

        if (hexCode.isEmpty()) {
            hexCode = "#";
            setCaretAndSelection();
        }

        super.frame(parentX, parentY, mouseX, mouseY, scrollY);
    }

    protected void parseText() {
        val argb = Color.parseHex(hexCode, includeAlpha);
        if (argb.isEmpty()) return;

        val targetColor = colorPopup.getTargetColor();
        targetColor.setToColor(argb.get());
    }
}
