package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.function.BooleanSupplier;

@Getter
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;
    private final String unit;
    @Setter
    private String stringValue;

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment,
                         BooleanSupplier supplier) {
        super(name, supplier);
        this.min = min;
        this.max = max;
        this.defaultValue = defaultvalue;
        this.value = defaultvalue;
        this.stringValue = this.getValueAsString();
        this.increment = increment;
        this.unit = "";
    }

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment, String unit,
                         BooleanSupplier supplier) {
        super(name, supplier);
        this.min = min;
        this.max = max;
        this.defaultValue = defaultvalue;
        this.value = defaultvalue;
        this.stringValue = this.getValueAsString();
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment, String unit) {
        super(name, null);
        this.min = min;
        this.max = max;
        this.defaultValue = defaultvalue;
        this.value = defaultvalue;
        this.stringValue = this.getValueAsString();
        this.increment = increment;
        this.unit = unit;
    }

    public NumberSetting(String name, double min, double max, double defaultvalue, double increment) {
        super(name, null);
        this.min = min;
        this.max = max;
        this.defaultValue = defaultvalue;
        this.value = defaultvalue;
        this.stringValue = this.getValueAsString();
        this.increment = increment;
        this.unit = "";
    }

    public void setValue(double value) {
        //double rounded = Math.round((value / increment)) * increment;
        this.value = Math.max(min, Math.min(max, value));
    }

    public String getValueAsString() {
        return (value % 1 == 0) ? new DecimalFormat("#").format(value) : new DecimalFormat("#.##").format(value);
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
