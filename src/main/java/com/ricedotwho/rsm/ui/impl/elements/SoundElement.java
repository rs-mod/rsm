package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SoundElement extends Widget {
    public SoundElement(Runnable playFunction, Supplier<String> supplier, Consumer<String> consumer, Supplier<Double> volumeSupplier, Supplier<Double> pitchSupplier, Consumer<Double> volumeConsumer, Consumer<Double> pitchConsumer) {
        var node = new RectangleNode.Builder()
                .width(Palette.largeElementWidth)
                .height(Palette.largeElementHeight)
                .gap(8f)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .alignItems(Align.CENTER)
                .justifyContent(JustifyContent.FLEX_START)
                .build();
        super(node);

        var textBoxNode = new YogaNodeBuilder()
                .width(152f)
                .height(Palette.largeElementHeight)
                .padding(Palette.elementInteriorPadding)
                .build();

        node.addChild(new TextBox(textBoxNode, supplier, consumer));
        node.addChild(new NumberBox(30f, 0, 2, 1, pitchSupplier, pitchConsumer, ""));
        node.addChild(new NumberBox(30f, 0, 1, 1, volumeSupplier, volumeConsumer, ""));
        node.addChild(new ButtonElement(60f, playFunction, "Play"));
    }
}
