package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.Getter;
import lombok.val;

class ColorDifference extends Widget {
    private static final Image colorDifferenceBackground = NVGUtils.createImage("/assets/rsm/clickgui/color_difference_background.png");
    @Getter
    private final RectangleNode previous;
    @Getter
    private final RectangleNode current;
    private final ColorPopup popupElement;

    public ColorDifference(ColorPopup popupElement) {
        val base = new RectangleNode.Builder()
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .justifyContent(Node.JustifyContent.FLEX_START)
                .gap(0)
                .padding(Palette.elementInteriorPadding)
                .alignItems(Align.CENTER)
                .build();
        super(base);


        val filling = new ImageNode.Builder()
                .width(66)
                .height(Palette.elementHeight)
                .positionType(PositionType.ABSOLUTE)
                .left(Palette.elementInteriorPadding)
                .top(Palette.elementInteriorPadding + 1) //idk why + 1, probably some rounding shit, but I can't be asked to do anything about it
                .image(colorDifferenceBackground)//too lazy to surround with another flex box
                .build();

        previous = new RectangleNode.Builder()
                .width(33)
                .height(Palette.elementHeight)
                .color(Palette.createColorContainer())
                .build();
        current = new RectangleNode.Builder()
                .width(33)
                .height(Palette.elementHeight)
                .color(Palette.createColorContainer())
                .build();
        this.popupElement = popupElement;

        base.addChild(filling);
        base.addChild(previous);
        base.addChild(current);
    }

    @Override
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        current.getColor().setToColor(popupElement.getTargetColor());
    }

    public void capture() {
        previous.getColor().setToColor(popupElement.getTargetColor());
    }
}
