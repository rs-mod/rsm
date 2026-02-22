package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BooleanSupplier;

@Getter
public class NumberSetting extends Setting<BigDecimal> {
    private final BigDecimal min;
    private final BigDecimal max;
    private final BigDecimal increment;
    private final String unit;
    @Setter
    private String stringValue;

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment,
                         BooleanSupplier supplier) {
        super(name, supplier);
        this.min = BigDecimal.valueOf(min);
        this.max = BigDecimal.valueOf(max);
        this.defaultValue = BigDecimal.valueOf(defaultvalue);
        this.value = BigDecimal.valueOf(defaultvalue);
        this.stringValue = this.getValueAsString();
        this.increment = BigDecimal.valueOf(increment);
        this.unit = "";
    }

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment, String unit,
                         BooleanSupplier supplier) {
        super(name, supplier);
        this.min = BigDecimal.valueOf(min);
        this.max = BigDecimal.valueOf(max);
        this.defaultValue = BigDecimal.valueOf(defaultvalue);
        this.value = BigDecimal.valueOf(defaultvalue);
        this.stringValue = this.getValueAsString();
        this.increment = BigDecimal.valueOf(increment);
        this.unit = unit;
    }

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment, String unit) {
        super(name, null);
        this.min = BigDecimal.valueOf(min);
        this.max = BigDecimal.valueOf(max);
        this.defaultValue = BigDecimal.valueOf(defaultvalue);
        this.value = BigDecimal.valueOf(defaultvalue);
        this.stringValue = this.getValueAsString();
        this.increment = BigDecimal.valueOf(increment);
        this.unit = unit;
    }

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment) {
        super(name, null);
        this.min = BigDecimal.valueOf(min);
        this.max = BigDecimal.valueOf(max);
        this.defaultValue = BigDecimal.valueOf(defaultvalue);
        this.value = BigDecimal.valueOf(defaultvalue);
        this.stringValue = this.getValueAsString();
        this.increment = BigDecimal.valueOf(increment);
        this.unit = "";
    }

    public void setValue(double value) {
        //double rounded = Math.round((value / increment)) * increment;
        this.value = BigDecimal.valueOf(value).max(this.min).min(this.max);
    }

    public void setValue(String value) {
        //double rounded = Math.round((value / increment)) * increment;
        this.value = new BigDecimal(value).max(this.min).min(this.max);
    }

    public String getValueAsString() {
        return value.stripTrailingZeros()
                .setScale(Math.min(2, Math.max(0, value.stripTrailingZeros().scale())), RoundingMode.HALF_UP)
                .toPlainString();
    }
}
