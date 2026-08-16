package com.ricedotwho.rsm.module.api.settings.impl;

import com.ricedotwho.rsm.module.api.settings.Setting;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class ButtonSetting extends Setting<String> {
    private final Runnable action;

    public ButtonSetting(String name, String defaultValue, BooleanSupplier supplier, Runnable action, String description) {
        super(name, supplier, null, description, null);
        this.value = defaultValue;
        this.defaultValue = value;
        this.action = action;
    }

    public ButtonSetting(String name, String defaultValue, BooleanSupplier supplier, Runnable action) {
        this(name, defaultValue, supplier, action, "");
    }

    public ButtonSetting(String name, String defaultValue, Runnable action, String description) {
        this(name, defaultValue, null, action, description);
    }

    public ButtonSetting(String name, String defaultValue, Runnable action) {
        this(name, defaultValue, null, action, "");
    }

    @Override
    public void resetToDefault() {

    }

    @Override
    public void readFromJson(JsonObject obj) {
        // no impl
    }

    @Override
    public void writeToJson(JsonObject obj) {
        // no impl
    }

    @Override
    public String getType() {
        return "button";
    }

    @Override
    public boolean savesToConfig() {
        return false;
    }
}
