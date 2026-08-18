package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean value, Runnable onRun, BooleanSupplier supplier, String description) {
        super(name, supplier, onRun, description, value);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, value);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value, String description) {
        super(name, null, null, description, value);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value, Runnable onRun, BooleanSupplier supplier) {
        super(name, supplier, onRun, "", value);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value, BooleanSupplier supplier) {
        super(name, supplier, null, "", value);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value) {
        super(name, null, null, "", value);
        this.value = value;
    }

    public void toggle(){
        value = !value;
    }

    @Override
    public void readFromJson(JsonObject obj) {
        setValue(obj.get("value").getAsBoolean());
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue());
    }

    @Override
    public String getType() {
        return "boolean";
    }
}
