package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SaveElement extends Widget {
    public SaveElement(Runnable loadFunction, Supplier<String> supplier, Consumer<String> consumer) {
        val node = new RectangleNode.Builder()
                .width(Palette.largeElementWidth)
                .height(Palette.largeElementHeight)
                .gap(8f)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .alignItems(Align.CENTER)
                .justifyContent(JustifyContent.FLEX_START)
                .build();
        super(node);

        val textBoxNode = new YogaNodeBuilder()
                .width(Palette.mediumElementWidth)
                .height(Palette.largeElementHeight)
                .padding(Palette.elementInteriorPadding)
                .build();

        node.addChild(new TextBox(textBoxNode, supplier, consumer));

        val button = new ButtonElement(loadFunction, "Load");
        node.addChild(button);
    }
}
