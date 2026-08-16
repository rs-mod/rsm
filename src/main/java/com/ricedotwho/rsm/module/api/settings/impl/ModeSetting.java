package com.ricedotwho.rsm.module.api.settings.impl;

import com.ricedotwho.rsm.module.api.settings.Setting;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
@SuppressWarnings("unused")
public class ModeSetting extends Setting<String> {
    private final ArrayList<String> values;

    public ModeSetting(String name, String defaultValue, List<String> modes, Runnable onEdit, BooleanSupplier supplier, String description) {
        super(name, supplier, onEdit, description, defaultValue);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, defaultValue);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes, String description) {
        super(name, null, null, description, defaultValue);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes, Runnable onEdit, BooleanSupplier supplier) {
        super(name, supplier, onEdit, "", defaultValue);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes, BooleanSupplier supplier) {
        super(name, supplier, null, "", defaultValue);
        this.value = defaultValue;
        this.values = new ArrayList<>(modes);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes) {
        super(name, null, null, "", defaultValue);
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

    public boolean inRange(int min, int max) {
        int index = getIndex();
        return min < index && max > index;
    }

    public boolean inRangeInclusive(int min, int max) {
        int index = getIndex();
        return min <= index && max >= index;
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

    @Override
    public void readFromJson(JsonObject obj) {
        this.setValue(obj.get("value").getAsString());
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue());
    }

    @Override
    public String getType() {
        return "mode";
    }
}
