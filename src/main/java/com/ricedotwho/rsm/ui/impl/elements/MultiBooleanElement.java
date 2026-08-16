package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import com.ricedotwho.rsm.ui.impl.popups.impl.dropdown.DropDownOption;
import com.ricedotwho.rsm.ui.impl.popups.impl.dropdown.DropDownPopup;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MultiBooleanElement extends ClickHandler {
    public static final Image downArrow = NVGUtils.createImage("/assets/rsm/clickgui/down_arrow.png");
    private static final Image upArrow = NVGUtils.createImage("/assets/rsm/clickgui/up_arrow.png");

    private final @Nullable Runnable onEdit;
    public MultiBooleanElement(Map<String, Boolean> optionsMap, @Nullable Runnable onEdit) {
        val node = new RectangleNode.Builder()
                .color(Palette.createColorContainer())
                .height(Palette.largeElementHeight)
                .width(Palette.largeElementWidth)
                .paddingLeft(8f)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.FLEX_START)
                .alignItems(Align.CENTER)
                .flexShrink(1f)
                .gap(0f)
                .build();

        super(node, true, false);

        val shrink = 8f;

        val arrowNodeContainer = new RectangleNode.Builder()
                .width(Palette.largeElementHeight)
                .height(Palette.largeElementHeight)
                .display(Display.FLEX)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.CENTER)
                .build();

        arrowNode = new ImageNode.Builder()
                .height(Palette.largeElementHeight - shrink)
                .width(Palette.largeElementHeight - shrink)
                .build();

        arrowNodeContainer.addChild(arrowNode);

        ArrayList<Option> array = new ArrayList<>(optionsMap.size());
        for (Map.Entry<String, Boolean> stringBooleanEntry : optionsMap.entrySet()) {
            array.add(new Option(stringBooleanEntry.getKey(), stringBooleanEntry::setValue, stringBooleanEntry::getValue));
        }
        this.optionNodes = array;
        updateText();


        this.textNode = new TextNode.Builder()
                .color(Palette.createColorContainer())
                .font(Palette.font)
                .fontSize(Palette.fontSize)
                .text(text)
                .align(TextAlignment.CenterLeft)
                .height(Palette.largeElementHeight)
                .truncateTextToFit(true)
                .width(Palette.largeElementWidth - Palette.largeElementHeight - 8f)
                .build();
        this.addChild(textNode);
        this.addChild(arrowNodeContainer);
        this.onEdit = onEdit;
    }

    private final ImageNode arrowNode;
    private final ArrayList<Option> optionNodes;
    private final TextNode textNode;
    private boolean requestOpen = false;
    private boolean open = false;
    private String text;

    private void updateText() {
        val builder = new StringBuilder();
        for (Option optionNode : optionNodes) {
            if (!optionNode.supplier.get()) continue;
            if (!builder.isEmpty()) builder.append(", ");
            builder.append(optionNode.text);
        }
        text = builder.toString();
    }

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.frame(parentX, parentY, mouseX, mouseY, scrollY);
        if (requestOpen) {
            DropDownPopup.open(originX(parentX), originY(parentY) + layoutHeight(), scrollY, optionNodes, () -> {
                if (onEdit != null) onEdit.run();
                open = false;
            });
            open = true;
            requestOpen = false;
        }
    }

    @Override
    protected void onRender(boolean hovered) {
        arrowNode.setImage(open ? upArrow : downArrow);
        node.getColor().setToColor(Palette.elementBackgroundLight.darker(getClickedAnimationContribution()));
        textNode.setText(text);
        textNode.getColor().setToColor(Palette.text.darker(getClickedAnimationContribution()));
    }

    @Override
    protected void onLeftTriggered() {
        requestOpen = true;
    }

    @Override
    public void close() {
        super.close();
        for (Option optionNode : optionNodes) {
            optionNode.close();
        }
    }

    private class Option extends DropDownOption {
        Consumer<Boolean> consumer;
        Supplier<Boolean> supplier;
        String text;
        public Option(String text, Consumer<Boolean> consumer, Supplier<Boolean> supplier) {
            super(text);
            this.consumer = consumer;
            this.text = text;
            this.supplier = supplier;
        }

        @Override
        protected void onRender(boolean hovered) {
            val t = hoverAnimation.get(0f, 0.1f, !hovered);
            val contribution = getClickedAnimationContribution();
            node.getColor().setToColor(Palette.elementBackgroundLight.adjustBrightness(t - contribution - 0.1f));

            val textColor = supplier.get() ? Palette.elementHighlight : Palette.text;
            textNode.getColor().setToColor(textColor.darker(contribution));
        }

        @Override
        protected void onLeftTriggered() {
            consumer.accept(!supplier.get());
            updateText();
        }
    }
}
