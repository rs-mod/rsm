package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import lombok.Getter;

@Getter
public class StringSetting extends Setting<String> {
    private final boolean allowBlank;
    private final boolean secure;
    private final int maxLength;

    public StringSetting(String name, String defaultValue, boolean allowBlank = true, boolean secure = false, int maxLength = 32, String description = "") {
        super(name, description, defaultValue);
        this.value = defaultValue;
        this.allowBlank = allowBlank;
        this.secure = secure;
        this.maxLength = maxLength;
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
        return "string";
    }
}
