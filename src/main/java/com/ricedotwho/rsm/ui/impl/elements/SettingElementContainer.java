package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.api.UiElement;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.clickgui.Contents;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.BooleanSupplier;

public class SettingElementContainer extends Node {
    public static Color elementStrokeColor = Palette.createColorContainer();
    @Nullable public BooleanSupplier supplier;
    private final ClickGui clickGui;
    private final Contents contents;
    private boolean lastVisible = true;

    protected SettingElementContainer(ClickGui clickGui, @Nullable BooleanSupplier supplier, Contents contents) {
        val n = new YogaNodeBuilder()
                .height(28f)
                .widthPercent(100)
                .build();

        Yoga.YGNodeStyleSetFlexDirection(n, FlexDirection.ROW.yg());
        super(n, null);
        this.supplier = supplier;
        this.clickGui = clickGui;
        this.contents = contents;
    }

    public SettingElementContainer(String name, @Nullable BooleanSupplier supplier, ClickGui clickGui, Contents contents, UiElement... uiElements) {
        val n = new YogaNodeBuilder()
                .height(28f)
                .widthPercent(100)
                .build();

        Yoga.YGNodeStyleSetFlexDirection(n, FlexDirection.ROW.yg());

        super(n, null);

        val text = new TextNode.Builder()
                .color(Palette.text)
                .align(TextAlignment.CenterLeft)
                .font(Palette.font)
                .fontSize(Palette.fontSizeLarge)
                .text(name)
                .heightPercent(100f)
                .left(0f)
                .build();

        val settingContainer = new RectangleNode.Builder()
                .alignItems(Align.CENTER)
                .justifyContent(JustifyContent.FLEX_END)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .flexGrow(1f)
                .flexShrink(1f)
                .right(0f)
                .paddingLeft(8f)
                .gap(8f)
                .build();

        val stroke = new RectangleNode.Builder()
                .height(Palette.strokeThickness)
                .color(elementStrokeColor)
                .flexGrow(1f)
                .build();


        this.addChild(text);
        settingContainer.addChild(stroke);
        for (UiElement element : uiElements) {
            settingContainer.addChild(element);
        }

        this.addChild(settingContainer);
        this.supplier = supplier;
        this.clickGui = clickGui;
        this.contents = contents;
    }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        val visible = supplier == null || supplier.getAsBoolean();
        if (!visible) {
            Yoga.YGNodeStyleSetDisplay(yogaNode, Yoga.YGDisplayNone);
        } else {
            Yoga.YGNodeStyleSetDisplay(yogaNode, Yoga.YGDisplayFlex);
        }

        super.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);

        setVisible(visible);
        if (lastVisible != visible) contents.requestRefresh();
        lastVisible = visible;
    }
}
