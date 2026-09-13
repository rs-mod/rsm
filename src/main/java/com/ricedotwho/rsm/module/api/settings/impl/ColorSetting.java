package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@SuppressWarnings("unused")
public class ColorSetting extends Setting<Color> {
    @Setter
    private Color value;
    private final Color defaultValue;

    public ColorSetting(String name, Color defaultValue, String description = "") {
        super(name, description, defaultValue);
        this.value = defaultValue.clone();
        this.defaultValue = defaultValue.clone();
    }

    @Override
    public void resetToDefault() {
        this.value.setToColor(defaultValue);
    }

    @Override
    public void readFromJson(JsonObject obj) {
        if (obj.has("hsba")) {
            JsonArray hsba = obj.get("hsba").getAsJsonArray();
            value.setHSV(hsba.get(0).getAsFloat(), hsba.get(1).getAsFloat(), hsba.get(2).getAsFloat(), hsba.get(3).getAsFloat());
        } else {
            val potentialARGB = Color.parseHex(obj.get("hex").getAsString(), true);
            potentialARGB.ifPresent(value::setToColor);
        }
    }


    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("hex", this.getValue().getHexCode(true));
    }

    @Override
    public String getType() {
        return "color";
    }
}
