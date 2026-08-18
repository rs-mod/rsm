package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.managers.KeybindManager;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Keybind;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class KeybindSetting extends Setting<Keybind> {
    @Getter
    private final boolean persistent;

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, boolean persistent, Runnable onEdit, BooleanSupplier supplier) {
        super(name, supplier, onEdit, "", new Keybind(key));
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, boolean persistent, BooleanSupplier supplier) {
        super(name, supplier, null, "", new Keybind(key));
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, BooleanSupplier supplier) {
        super(name, supplier, null, "", new Keybind(key));
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action) {
        super(name, null, null, "", new Keybind(key));
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key) {
        super(name, null, null, "", new Keybind(key));
        this.persistent = false;
        this.value = key;
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, boolean persistent, Runnable onEdit, BooleanSupplier supplier, String description) {
        super(name, supplier, onEdit, description, new Keybind(key));
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, boolean persistent, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, new Keybind(key));
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, new Keybind(key));
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, String description) {
        super(name, null, null, description, new Keybind(key));
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, String description) {
        super(name, null, null, description, new Keybind(key));
        this.persistent = false;
        this.value = key;
    }

    public boolean isRegistered() {
        return KeybindManager.getKeyBinds().contains(value);
    }

    @Override
    public void readFromJson(JsonObject obj) {
        JsonElement keyObj = obj.get("value");
        String key = keyObj == null ? "key.keyboard.unknown" : keyObj.getAsString();
        this.value.setKey(InputConstants.getKey(key));
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue().getKey().getName());
    }

    @Override
    public String getType() {
        return "keybind";
    }
}
