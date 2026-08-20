package com.ricedotwho.rsm.ui.impl.clickgui.topbar;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import org.jetbrains.annotations.NotNull;

public class CategoryButton extends ClickHandler {
    public CategoryButton(@NotNull Category category, ClickGui clickGui) {
        super(
                new RectangleNode.Builder()
                        .display(Node.Display.FLEX)
                        .flexDirection(Node.FlexDirection.ROW)
                        .alignItems(Node.Align.CENTER)
                        .justifyContent(Node.JustifyContent.CENTER)
                        .height(36)
                        .width(128)
                        .rounding(5)
                        .color(Color.black.clone()) //this can be any color, it will be overridden
                        .build(),
                true,
                false
        );
        this.clickGui = clickGui;

        var text = new TextNode.Builder()
                .text(category.getName())
                .align(TextAlignment.CenterMiddle)
                .fontSize(Palette.fontSize)
                .font(Palette.fontBold)
                .color(Palette.text)
                .build();

        node.addChild(text);
        this.category = category;
    }

    private final ClickGui clickGui;
    private final Category category;

    @Override
    protected void onLeftTriggered() {
        clickGui.currentCategory = category;
        clickGui.getSideBar().getModuleButtonContainer().resetScroll();
    }

    @Override
    protected void onRender(boolean hovered) {
        boolean selected = category == clickGui.currentCategory;
        setInteractable(!selected);

        float alpha = selected ? 1f : getClicked() ? 0.6f : hoverAnimation.get(0f, 0.6f, !hovered);
        int color;
        if (!selected) {
            color = Color.setArgbAlpha(Palette.stroke.darker(getClickedAnimationContribution()), alpha);
        } else {
            color = Color.setArgbAlpha(Palette.stroke.getARGB(), alpha);
        }

        node.getColor().setToColor(color);
    }
}
