package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.utils.MathUtils;
import lombok.val;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderElement extends Widget {
    private final Consumer<Double> consumer;
    private final Supplier<Double> supplier;

    final double increment, max, min;

    private final Node filling;

    public SliderElement(Consumer<Double> consumer, Supplier<Double> supplier, double min, double max, double increment) {
        val base = new RectangleNode.Builder()
                .maxWidth(220)
                .widthPercent(100)
                .height(Palette.elementHeight)
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .justifyContent(Node.JustifyContent.FLEX_START)
                .padding(Palette.elementInteriorPadding)
                .alignItems(Node.Align.STRETCH)
                .build();

        super(base);

        this.filling = new RectangleNode.Builder()
                .maxWidth(220 - Palette.elementInteriorPadding * 2f)
                .maxHeight(Palette.elementHeight - Palette.elementInteriorPadding * 2f)
                .heightPercent(100)
                .widthPercent(100)
                .color(Palette.elementHighlight)
                .build();

        base.addChild(filling);
        this.consumer = consumer;
        this.supplier = supplier;
        this.max = max;
        this.min = min;
        this.increment = increment;
        this.visualValue = supplier.get();
    }

    private float lastPercent = -1;
    private long lastMs = -1;
    private boolean dragging = false;
    private double visualValue;
    private double lastSetValue = Double.MAX_VALUE;

    @Override
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (lastSetValue != supplier.get()) visualValue = supplier.get();

        float targetSliderWidth = (float) ((visualValue - min) / (max - min));

        if (lastPercent == -1) lastPercent = targetSliderWidth;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastMs) / 1000.0f;
        lastMs = currentTime;

        float smoothingSpeed = 12.0f;
        lastPercent += (targetSliderWidth - lastPercent) * Math.min(1.0f, deltaTime * smoothingSpeed);

        if (Math.abs(lastPercent - targetSliderWidth)  < 0.002f) {
            lastPercent = targetSliderWidth;
        }

        filling.setVisible(lastPercent != 0);
        filling.setWidthPercent(lastPercent * 100);

        if (!dragging) return;
        float mouseOffset = mouseX - layoutLeft() - parentX - Palette.strokeThickness * 3f;
        double newPercent = Math.clamp(mouseOffset / (layoutWidth() - Palette.strokeThickness * 6f), 0, 1);
        double newValue = min + newPercent * (max - min);
        visualValue = newValue;

        if (increment != 0) {
            newValue = MathUtils.truncate(newValue, 2);
        }
        consumer.accept(newValue);
        lastSetValue = supplier.get();

    }

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (button != 0 || !isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;
        dragging = true;
        return true;
    }

    @Override
    protected void mouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        dragging = false;
    }
}
