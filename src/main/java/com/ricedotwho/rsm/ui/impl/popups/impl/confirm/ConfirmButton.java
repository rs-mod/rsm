package com.ricedotwho.rsm.ui.impl.popups.impl.confirm;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.Setter;
import lombok.val;

public class ConfirmButton extends ClickHandler {
    private final RectangleNode baseNode;
    private final TextNode textNode;
    private final ConfirmPopup popup;

    @Setter
    private Runnable runnable;
    public ConfirmButton(ConfirmPopup popup) {
        val node = new RectangleNode.Builder()
                .width(160)
                .height(Palette.largeElementHeight * 1.5f)
                .color(Palette.createColorContainer())
                .rounding(5f)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.CENTER)
                .build();

        super(node, true, false);
        baseNode = node;
        textNode = new TextNode.Builder()
                .flexGrow(1f)
                .color(Palette.createColorContainer())
                .text("Confirm")
                .shadow(true)
                .font(Palette.fontBold)
                .fontSize(Palette.fontSize)
                .align(TextAlignment.CenterMiddle)
                .positionType(PositionType.ABSOLUTE)
                .build();

        node.addChild(textNode);
        this.popup = popup;
    }

    @Override
    protected void onRender(boolean hovered) {
        baseNode.getColor().setToColor(Palette.elementHighlight.darker(getClickedAnimationContribution() * 2f));
        textNode.getColor().setToColor(Palette.text.darker(getClickedAnimationContribution() * 2f));
    }

    @Override
    protected void onLeftTriggered() {
        runnable.run();
        popup.setVisible(false);
    }
}
