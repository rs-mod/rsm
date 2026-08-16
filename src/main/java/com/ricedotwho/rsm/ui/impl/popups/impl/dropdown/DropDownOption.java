package com.ricedotwho.rsm.ui.impl.popups.impl.dropdown;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.val;

abstract public class DropDownOption extends ClickHandler {
    protected final TextNode textNode;
    public DropDownOption(String text) {
        val node = new RectangleNode.Builder()
                .alignItems(Align.CENTER)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.FLEX_START)
                .display(Display.FLEX)
                .widthPercent(100)
                .height(Palette.largeElementHeight)
                .color(Palette.createColorContainer())
                .paddingLeft(8f)
                .build();
        super(node, true, false);
        textNode = new TextNode.Builder()
                .flexGrow(1f)
                .shadow(false)
                .text(text)
                .fontSize(Palette.fontSize)
                .font(Palette.font)
                .color(Palette.createColorContainer())
                .build();
        this.addChild(textNode);
    }

    @Override
    protected void onRender(boolean hovered) {
        val t = hoverAnimation.get(0f, 0.1f, !hovered);
        val contribution = getClickedAnimationContribution();
        node.getColor().setToColor(Palette.elementBackgroundLight.adjustBrightness(t - contribution - 0.1f)); //
        textNode.getColor().setToColor(Palette.text.darker(contribution));
    }
}
