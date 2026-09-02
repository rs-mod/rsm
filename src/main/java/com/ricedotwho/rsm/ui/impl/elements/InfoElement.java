package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.*;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.clickgui.Contents;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class InfoElement extends SettingElementContainer {
    public InfoElement(String name, Color color, Color lineColour, @Nullable BooleanSupplier supplier, ClickGui clickGui, Contents contents) {
        super(clickGui, supplier, contents);

        var settingContainer = new RectangleNode.Builder()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .alignItems(Align.STRETCH)
                .justifyContent(JustifyContent.FLEX_START)
                .flexGrow(1f)
                .flexShrink(1f)
                .build();

        var text = new TextNode.Builder()
                .color(color)
                .align(TextAlignment.CenterMiddle)
                .font(Palette.font)
                .fontSize(Palette.fontSizeLarge)
                .text(name)
                .flexGrow(1f)
                .build();

        var top = new RectangleNode.Builder()
                .height(Palette.strokeThickness)
                .color(lineColour)
                .marginLeft(-10f)
                .marginRight(-10f)
                .build();

        var bottom = new RectangleNode.Builder()
                .height(Palette.strokeThickness)
                .color(lineColour)
                .marginLeft(-10f)
                .marginRight(-10f)
                .build();

        settingContainer.addChild(top);
        settingContainer.addChild(text);
        settingContainer.addChild(bottom);

        this.addChild(settingContainer);
    }
}
