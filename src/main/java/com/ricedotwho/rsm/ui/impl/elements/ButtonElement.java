package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.val;

public class ButtonElement extends ClickHandler {

    Runnable run;
    private final TextNode text;
    public ButtonElement(float width, Runnable run, String text) {
        val base = new RectangleNode.Builder()
                .color(Palette.createColorContainer())
                .padding(Palette.elementInteriorPadding)
                .width(width)
                .height(Palette.largeElementHeight)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.CENTER)
                .alignContent(Align.STRETCH)
                .build();

        super(base, true, false);
        this.text = new TextNode.Builder()
                .align(TextAlignment.CenterMiddle)
                .font(Palette.font)
                .fontSize(Palette.fontSize)
                .text(text)
                .shadow(false)
                .flexGrow(1f)
                .color(Palette.createColorContainer())
                .build();

        this.addChild(this.text);

        this.run = run;
    }

    public ButtonElement(Runnable run, String text) {
        this(68, run, text);
    }

    @Override
    protected void onRender(boolean hovered) {
        val accentColor = Palette.elementBackgroundLight.darker(getClickedAnimationContribution());
        val textColor = Palette.text.darker(getClickedAnimationContribution());

        node.getColor().setToColor(accentColor);
        text.getColor().setToColor(textColor);
    }

    @Override
    protected void onLeftTriggered() {
        run.run();
    }
}
