package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

public class KeybindSetting extends Setting<Keybind> {
    @Getter
    private final boolean persistent;

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, boolean persistent, Runnable onEdit, BooleanSupplier supplier) {
        super(name, supplier, onEdit);
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, boolean persistent, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, BooleanSupplier action) {
        super(name, null, null);
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key) {
        super(name, null, null);
        this.persistent = false;
        this.value = key;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        JsonElement keyObj = obj.get("value");
        String key = keyObj == null ? "key.keyboard.unknown" : keyObj.getAsString();
        this.value.setKeyBind(InputConstants.getKey(key));
    }

    @Override
    public void saveToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue().getKeyBind().getName());
    }

    @Override
    public String getType() {
        return "keybind";
    }
}
