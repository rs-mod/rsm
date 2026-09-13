package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import lombok.Getter;

@Getter
public class ButtonSetting extends Setting<String> {
    private Runnable action;

    public ButtonSetting(String name, String buttonText, Runnable action = () -> {}, String description = "") {
        super(name, description, buttonText);
        this.value = buttonText;
        this.action = action;
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
