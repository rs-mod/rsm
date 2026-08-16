package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CheckMark extends ClickHandler {
    private final Consumer<Boolean> consumer;
    private final Supplier<Boolean> supplier;

    public final Node checkMark;

    public CheckMark(Consumer<Boolean> booleanConsumer, Supplier<Boolean> supplier) {
        val base = new RectangleNode.Builder()
                .height(Palette.elementHeight)
                .aspectRatio(1f)
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.createColorContainer())
                .display(Node.Display.FLEX)
                .justifyContent(Node.JustifyContent.CENTER)
                .alignItems(Node.Align.STRETCH)
                .padding(Palette.elementInteriorPadding)
                .build();

        super(base, true, false);

        this.checkMark = new RectangleNode.Builder()
                .color(Palette.createColorContainer())
                .flexGrow(1f)
                .build();

        this.node.addChild(checkMark);
        this.consumer = booleanConsumer;
        this.supplier = supplier;
    }

    @Override
    protected void onRender(boolean hovered) {
        val strokeColor = Palette.stroke.darker(getClickedAnimationContribution());
        val accentColor = Palette.elementHighlight.darker(getClickedAnimationContribution());

        assert this.node.getColor() != null;
        ((RectangleNode) this.node).getOutlineColor().setToColor(strokeColor);

        assert checkMark.getColor() != null;
        checkMark.getColor().setToColor(accentColor);

        checkMark.setVisible(supplier.get());
    }

    @Override
    protected void onLeftTriggered() {
        consumer.accept(!supplier.get());
    }
}
