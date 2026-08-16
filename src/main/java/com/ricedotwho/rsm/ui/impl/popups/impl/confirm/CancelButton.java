package com.ricedotwho.rsm.ui.impl.popups.impl.confirm;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.val;

public class CancelButton extends ClickHandler {
    private final RectangleNode baseNode;
    private final TextNode textNode;
    public CancelButton() {
        val node = new RectangleNode.Builder()
                .width(160)
                .height(Palette.largeElementHeight * 1.5f)
                .outline(2f, Palette.createColorContainer())
                .color(Palette.createColorContainer())
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.CENTER)
                .rounding(5f)
                .build();

        super(node, true, false);
        baseNode = node;
        textNode = new TextNode.Builder()
                .flexGrow(1f)
                .color(Palette.createColorContainer())
                .text("Cancel")
                .font(Palette.fontBold)
                .fontSize(Palette.fontSize)
                .align(TextAlignment.CenterMiddle)
                .positionType(PositionType.ABSOLUTE)
                .build();

        node.addChild(textNode);
    }

    @Override
    protected void onRender(boolean hovered) {
        baseNode.getColor().setToColor(Palette.foreground.darker(getClickedAnimationContribution()));
        baseNode.getOutlineColor().setToColor(Palette.stroke.adjustBrightness(-getClickedAnimationContribution() + 0.2f));
        textNode.getColor().setToColor(Palette.stroke.adjustBrightness(-getClickedAnimationContribution() + 0.4f));
    }

    @Override
    protected void onLeftTriggered() {
        ConfirmPopup.getInstance().setVisible(false);
    }
}
