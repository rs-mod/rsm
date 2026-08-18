package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class StringSetting extends Setting<String> {
    private final boolean allowBlank;
    private final boolean secure;
    private final int maxLength;

    public StringSetting(String name, String defaultValue, BooleanSupplier supplier, boolean allowBlank, boolean secure, int maxLength, Runnable onEdit, String description) {
        super(name, supplier, onEdit, description, defaultValue);
        this.value = defaultValue;
        this.allowBlank = allowBlank;
        this.secure = secure;
        this.maxLength = maxLength;
    }

    public StringSetting(String name,  String defaultValue, BooleanSupplier supplier, boolean allowBlank, boolean secure, int maxLength, String description) {
        this(name, defaultValue, supplier, allowBlank, secure, maxLength, null, description);
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank, boolean secure, BooleanSupplier supplier, String description) {
        this(name, defaultValue, supplier, allowBlank, secure, 32, null, description);
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank, boolean secure, String description) {
        this(name, defaultValue, null, allowBlank, secure, 32, null, description);
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank, String description) {
        this(name, defaultValue, null, allowBlank, false, 32, null, description);
    }

    public StringSetting(String name, String defaultValue, String description) {
        this(name, defaultValue, null, true, false, 32, null, description);
    }

    public StringSetting(String name,String defaultValue, BooleanSupplier supplier, boolean allowBlank, boolean secure, int maxLength, Runnable onEdit) {
        super(name, supplier, onEdit, "", defaultValue);
        this.value = defaultValue;
        this.allowBlank = allowBlank;
        this.secure = secure;
        this.maxLength = maxLength;
    }

    public StringSetting(String name,String defaultValue, BooleanSupplier supplier, boolean allowBlank, boolean secure, int maxLength) {
        this(name, defaultValue, supplier, allowBlank, secure, maxLength, null, "");
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank, boolean secure, BooleanSupplier supplier) {
        this(name, defaultValue, supplier, allowBlank, secure, 32, null, "");
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank, boolean secure) {
        this(name, defaultValue, null, allowBlank, secure, 32, null, "");
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank) {
        this(name, defaultValue, null, allowBlank, false, 32, null, "");
    }

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, null, true, false, 32, null, "");
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
