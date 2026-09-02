package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NumberBox extends TextBox {
    private final String unit;
    public NumberBox(
            long yogaNode,
            double min,
            double max,
            int truncate,
            Supplier<Double> supplier,
            Consumer<Double> consumer,
            String unit
    ) {
        super(yogaNode, null, null, "", null, truncate == 0 ? "[^0-9]" : "[^0-9.]");
        setTextConsumer(this::setDisplay);
        setTextSupplier(this::getDisplay);
        setOnUnlisten(this::attemptAcceptDisplay);
        this.min = min;
        this.max = max;
        this.truncate = truncate;
        this.supplier = supplier;
        this.consumer = consumer;
        this.unit = unit;
        setDisplayFromSupplier();
        initState();
    }

    public NumberBox(
            double min,
            double max,
            int truncate,
            Supplier<Double> supplier,
            Consumer<Double> consumer,
            String unit
    ) {
        val yogaNode = new YogaNodeBuilder()
                .width(68)
                .height(Palette.largeElementHeight)
                .padding(Palette.elementInteriorPadding)
                .build();
        this(yogaNode, min, max, truncate, supplier, consumer, unit);
    }

    public NumberBox(long yogaNode, double min, double max, int truncate, Supplier<Double> supplier, Consumer<Double> consumer) {
        this(yogaNode, min, max, truncate, supplier, consumer, "");
    }

    public static Builder builder() { return new Builder(); }


    @Getter
    @Setter
    private String display = "";
    private final Supplier<Double> supplier;
    private final Consumer<Double> consumer;
    private final double min;
    private final double max;
    private final int truncate;

    private void setDisplayFromSupplier() {
        double value = MathUtils.truncate(supplier.get(), truncate);

        if (truncate <= 0) {
            display = (long) value + unit;
        } else {
            display = BigDecimal.valueOf(value)
                    .stripTrailingZeros()
                    .toPlainString() + unit;
        }
    }

    private void attemptAcceptDisplay() {
        if (display.isBlank()) {
            consumer.accept(min);
            setDisplayFromSupplier();
            return;
        }
        try {
            val value = Math.clamp(Double.parseDouble(display), min, max);
            consumer.accept(MathUtils.truncate(value, truncate));
        } catch (NumberFormatException _) {
        }
        setDisplayFromSupplier();
    }

    @Override
    protected void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isListening()) setDisplayFromSupplier();
        super.frame(parentX, parentY, mouseX, mouseY, scrollY);
    }
}
