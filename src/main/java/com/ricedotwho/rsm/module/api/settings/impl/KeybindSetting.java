package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.managers.KeybindManager;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Keybind;
import lombok.Getter;

@SuppressWarnings("unused")
public class KeybindSetting extends Setting<Keybind> {
    @Getter
    private final boolean persistent;

    public KeybindSetting(String name, Keybind key, boolean persistent = false, String description = "") {
        super(name, description, new Keybind(key));
        this.value = key;
        this.persistent = persistent;
    }

    public boolean isRegistered() {
        return KeybindManager.isRegistered(this.value);
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
