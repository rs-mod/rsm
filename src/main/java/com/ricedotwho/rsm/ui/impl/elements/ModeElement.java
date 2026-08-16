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

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModeElement extends ClickHandler {
    public static final Image downArrow = NVGUtils.createImage("/assets/rsm/clickgui/down_arrow.png");
    private static final Image upArrow = NVGUtils.createImage("/assets/rsm/clickgui/up_arrow.png");

    public ModeElement(String[] options, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        val node = new RectangleNode.Builder()
                .color(Palette.createColorContainer())
                .height(Palette.largeElementHeight)
                .width(Palette.largeElementWidth)
                .paddingLeft(8f)
                .build();

        super(node, true, false);

        val shrink = 8f;

        arrowNode = new ImageNode.Builder()
                .height(Palette.largeElementHeight - shrink)
                .width(Palette.largeElementHeight - shrink)
                .positionType(PositionType.ABSOLUTE)
                .right(shrink / 2)
                .top(shrink / 2)
                .build();

        this.supplier = supplier;
        ArrayList<Option> array = new ArrayList<>(options.length);
        for (int i = 0; i < options.length; i++) {
            array.add(new Option(options[i], i, consumer, supplier));
        }
        this.options = array;
        this.textNode = new TextNode.Builder()
                .color(Palette.createColorContainer())
                .font(Palette.font)
                .fontSize(Palette.fontSize)
                .text(options[supplier.get()])
                .align(TextAlignment.CenterLeft)
                .height(Palette.largeElementHeight)
                .build();
        this.addChild(textNode);
        this.addChild(arrowNode);
    }

    private final ImageNode arrowNode;
    private final ArrayList<Option> options;
    private final Supplier<Integer> supplier;
    private final TextNode textNode;
    private boolean requestOpen = false;
    private boolean open = false;

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.frame(parentX, parentY, mouseX, mouseY, scrollY);
        if (requestOpen) {
            DropDownPopup.open(originX(parentX), originY(parentY) + layoutHeight(), scrollY, options, () -> open = false);
            open = true;
            requestOpen = false;
        }
    }

    @Override
    protected void onRender(boolean hovered) {
        arrowNode.setImage(open ? upArrow : downArrow);
        node.getColor().setToColor(Palette.elementBackgroundLight.darker(getClickedAnimationContribution()));
        textNode.setText(options.get(supplier.get()).text);
        textNode.getColor().setToColor(Palette.text.darker(getClickedAnimationContribution()));
    }

    @Override
    public void close() {
        super.close();
        for (Option option : options) {
            option.close();
        }
    }

    @Override
    protected void onLeftTriggered() {
        requestOpen = true;
    }

    private static class Option extends DropDownOption {
        int index;
        Consumer<Integer> consumer;
        Supplier<Integer> supplier;
        String text;
        public Option(String text, int index, Consumer<Integer> consumer, Supplier<Integer> supplier) {
            super(text);
            this.index = index;
            this.consumer = consumer;
            this.text = text;
            this.supplier = supplier;
        }

        @Override
        protected void onRender(boolean hovered) {
            val t = hoverAnimation.get(0f, 0.1f, !hovered);
            val contribution = getClickedAnimationContribution();
            node.getColor().setToColor(Palette.elementBackgroundLight.adjustBrightness(t - contribution - 0.1f));

            val textColor = index == supplier.get() ? Palette.elementHighlight : Palette.text;
            textNode.getColor().setToColor(textColor.darker(contribution));
        }

        @Override
        protected void onLeftTriggered() {
            consumer.accept(index);
            DropDownPopup.closeMenu();
        }
    }
}
