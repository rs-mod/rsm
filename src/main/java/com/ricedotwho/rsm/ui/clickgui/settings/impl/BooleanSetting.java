package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean value, Runnable onRun, BooleanSupplier supplier) {
        super(name, supplier, onRun);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.value = value;
    }

    public BooleanSetting(String name, boolean value) {
        super(name, null, null);
        this.value = value;
    }

    public void toggle(){
        value = !value;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        setValue(obj.get("value").getAsBoolean());
    }

    @Override
    public void saveToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue());
    }

    @Override
    public String getType() {
        return "boolean";
    }
}
