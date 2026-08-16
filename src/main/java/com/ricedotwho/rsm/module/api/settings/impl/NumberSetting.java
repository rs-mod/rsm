package com.ricedotwho.rsm.module.api.settings.impl;

import com.ricedotwho.rsm.module.api.settings.Setting;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BooleanSupplier;

@Setter
@Getter
public class NumberSetting<E extends Number & Comparable<E>> extends Setting<E> {
    private E min;
    private E max;
    private E increment;
    private String unit;

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = "";
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, String unit, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, String unit, Runnable onEdit, BooleanSupplier supplier, String description) {
        super(name, supplier, onEdit, description, defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, String unit, String description) {
        super(name, null, null, description, defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, String description) {
        super(name, null, null, description, defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = "";
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, BooleanSupplier supplier) {
        super(name, supplier, null, "", defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = "";
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, String unit, BooleanSupplier supplier) {
        super(name, supplier, null, "", defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment, String unit, Runnable onEdit, BooleanSupplier supplier) {
        super(name, supplier, onEdit, "", defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, String unit, E min, E max, E defaultValue, E increment) {
        super(name, null, null, "", defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, E min, E max, E defaultValue, E increment) {
        super(name, null, null, "", defaultValue);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.increment = increment;
        this.unit = "";
    }

    public void setValue(E value) {
        this.value = computeSnapped(toBigDecimal(value));
    }

    public void setValue(double value) { this.value = computeSnapped(BigDecimal.valueOf(value)); }
    public void setValue(String value) {
        this.value = computeSnapped(new BigDecimal(value));
    }

    private E computeSnapped(BigDecimal raw) {
        val minBd = toBigDecimal(min);
        val maxBd = toBigDecimal(max);
        val incrementBd = toBigDecimal(increment);

        val steps = raw.subtract(minBd).divide(incrementBd, 0, RoundingMode.HALF_UP);
        val snapped = minBd.add(steps.multiply(incrementBd));
        val clamped = snapped.max(minBd).min(maxBd);

        return fromBigDecimal(clamped);
    }

    public BigDecimal getIncrementAsBigDecimal() {
        return toBigDecimal(increment);
    }

    private BigDecimal toBigDecimal(E value) {
        return switch (value) {
            case BigDecimal bd -> bd;
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            case Short s -> BigDecimal.valueOf(s);
            case Byte b -> BigDecimal.valueOf(b);
            case Double d -> BigDecimal.valueOf(d);
            case Float f -> new BigDecimal(Float.toString(f));
            default -> new BigDecimal(value.toString());
        };
    }

    private E fromBigDecimal(BigDecimal value) {
        return (E) switch (min) {
            case BigDecimal ignored -> value;
            case Integer ignored -> value.intValue();
            case Long ignored -> value.longValue();
            case Short ignored -> value.shortValue();
            case Byte ignored -> value.byteValue();
            case Double ignored -> value.doubleValue();
            case Float ignored -> value.floatValue();
            default -> throw new IllegalArgumentException(
                    "Unsupported number type: " + min.getClass().getSimpleName());
        };
    }

    @Override
    public void readFromJson(JsonObject obj) {
        this.setValue(obj.get("value").getAsString());
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", toBigDecimal(this.getValue()).toPlainString());
    }

    @Override
    public String getType() {
        return "number";
    }
}
