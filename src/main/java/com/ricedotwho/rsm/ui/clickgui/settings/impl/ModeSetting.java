package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
public class ModeSetting extends Setting<String> {

    private final ArrayList<String> values;

    public ModeSetting(String name, String defaultValue, List<String> modes, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes) {
        super(name, null);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public void setValue(String value) {
        this.value = value;
    }
    public void setByIndex(int index) {
        if(this.values.size() < index) return;
        this.value = this.values.get(index);
    }

    public String getValue() {
        return value;
    }

    public int getIndex() {
        return this.getValues().indexOf(this.value);
    }

    public void cycle() {
        int currentIndex = values.indexOf(getValue());
        int nextIndex = (currentIndex + 1) % values.size();
        setValue(values.get(nextIndex));
    }

    public void cycleBackwards() {
        int index = values.indexOf(value);
        if (index <= 0)
            index = values.size() - 1;
        else
            index--;
        value = values.get(index);
    }

    public boolean is(String other) {
        return this.value.equalsIgnoreCase(other);
    }
}
