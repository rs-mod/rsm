package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.val;

public class ButtonElement extends ClickHandler {
    public Color color;
    public Color textColor;
    Runnable run;
    private final TextNode text;
    public ButtonElement(Color color, Color textColor, float width, Runnable run, String text) {
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
        this.color = color;
        this.textColor = textColor;
    }

    public ButtonElement(float width, Runnable run, String text) {
        this(Palette.elementBackgroundLight, Palette.text, width, run, text);
    }

    public ButtonElement(Runnable run, String text) {
        this(Palette.elementBackgroundLight, Palette.text, 68, run, text);
    }

    @Override
    protected void onRender(boolean hovered) {
        node.getColor().setToColor(this.color.darker(getClickedAnimationContribution()));
        text.getColor().setToColor(this.textColor.darker(getClickedAnimationContribution()));
    }

    public void setText(String content) {
        this.text.setText(content);
    }

    public void setColor(Color color) {
        this.node.getColor().setToColor(color);
    }

    @Override
    protected void onLeftTriggered() {
        run.run();
    }
}
