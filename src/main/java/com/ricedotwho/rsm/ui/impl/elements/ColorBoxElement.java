package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector.ColorPopup;
import lombok.val;

public class ColorBoxElement extends ClickHandler {
    private final Color color;

    public ColorBoxElement(Color color) {
        val base = new RectangleNode.Builder()
                .padding(Palette.elementInteriorPadding)
                .height(Palette.largeElementHeight)
                .aspectRatio(1.5f)
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.STRETCH)
                .build();

        val filling = new RectangleNode.Builder()
                .flexGrow(1f)
                .color(color)
                .build();

        base.addChild(filling);
        super(base, true, false);
        this.color = color;
    }

    private boolean requestOpenPopup = false;

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.frame(parentX, parentY, mouseX, mouseY, scrollY);

        if (requestOpenPopup) {
            //the 4f is just to make it not blend into the setting.
            ColorPopup.openColorPopup(color, parentX + this.layoutLeft() - 4f - ColorPopup.precomputedWidth, parentY + this.layoutTop(), scrollY);
            requestOpenPopup = false;
        }
    }

    @Override
    protected void onLeftTriggered() {
        requestOpenPopup = true;
    }
}
