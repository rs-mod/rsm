package com.ricedotwho.rsm.ui.impl.other;

import com.ricedotwho.rsm.ui.api.*;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import net.minecraft.network.chat.Component;

public abstract class FeatureGui extends Gui {
    protected final RectangleNode container;
    public FeatureGui(String name) {
        super(Component.literal("RSM $name"),
                GuiAlignment.CenterMiddle,
                new RectangleNode.Builder()
                        .display(Node.Display.FLEX)
                        .flexDirection(Node.FlexDirection.COLUMN)
                        .maxWidth(1338 + 8)
                        .widthPercent(100)
                        .height(792)
                        .color(Palette.backdrop)
                        .rounding(10)
                        .build());

        var title = new TextNode.Builder()
                .align(TextAlignment.CenterLeft)
                .fontSize(Palette.titleFontSize)
                .text(name)
                .shadow(false)
                .font(Palette.fontBold)
                .height(36f)
                .width(100f)
                .left(20f)
                .positionType(Node.PositionType.ABSOLUTE)
                .color(Palette.text)
                .build();

        addTopBar(frame, title);

        this.container = createContents(frame);

        ClickGui.addBottomBar(frame);
    }

    protected RectangleNode createContents(Node frame) {
        var container = new RectangleNode.Builder()
                .display(UiElement.Display.FLEX)
                .flexDirection(UiElement.FlexDirection.COLUMN)
                .paddingLeft(8)
                .paddingRight(8)
                .paddingTop(8)
                .paddingBottom(8)
                .gap(8)
                .flexGrow(1f)
                .flexShrink(1f)
                .overflow(UiElement.Overflow.SCROLL)
                .build();

        container.calculateLayout(Float.NaN, Float.NaN);
        frame.addChild(container);
        return container;
    }

    protected void addTopBar(Node frame, TextNode title) {
        var topBar = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignItems(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.CENTER)
                .height(68)
                .color(Palette.foreground)
                .rounding(10, 10, 0, 0)
                .build();

        var stroke = new RectangleNode.Builder()
                .height(Palette.strokeThickness)
                .positionType(Node.PositionType.ABSOLUTE)
                .bottom(0)
                .widthPercent(100)
                .color(Palette.stroke)
                .build();

        topBar.addChild(title);
        topBar.addChild(stroke);
        frame.addChild(topBar);
    }
}
