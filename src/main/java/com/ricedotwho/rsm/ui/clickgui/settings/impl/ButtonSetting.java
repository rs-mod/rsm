package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class ButtonSetting extends Setting<String> {
    private final Runnable action;

    public ButtonSetting(String name, String defaultValue, BooleanSupplier supplier, Runnable action) {
        super(name, supplier, null);
        this.value = defaultValue;
        this.defaultValue = value;
        this.action = action;
    }

    public ButtonSetting(String name, String defaultValue, Runnable action) {
        this(name, defaultValue, null, action);
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        // no impl
    }

    @Override
    public void saveToJson(JsonObject obj) {
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
