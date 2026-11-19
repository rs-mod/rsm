package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import lombok.Setter;

@Getter
@SuppressWarnings("unused")
public class DualColorSetting extends Setting<Color> {
    @Setter
    private Color value;
    private final Color otherValue;
    private final Color defaultValue;
    private final Color defaultOtherValue;
    private final boolean valueAlpha;
    private final boolean otherValueAlpha;

    public DualColorSetting(String name, Color defaultValue, Color defaultOtherValue, boolean valueAlpha = true, boolean otherValueAlpha = true, String description = "") {
        super(name, description, defaultValue);
        this.value = defaultValue.clone();
        this.defaultValue = defaultValue.clone();
        this.otherValue = defaultOtherValue.clone();
        this.defaultOtherValue = defaultOtherValue.clone();
        this.valueAlpha = valueAlpha;
        this.otherValueAlpha = otherValueAlpha;
    }

    @Override
    public void resetToDefault() {
        this.value.setToColor(defaultValue);
        this.otherValue.setToColor(defaultOtherValue);
    }

    @Override
    public void readFromJson(JsonObject obj) {
        var hex1 = Color.parseHex(obj.get("hex1").getAsString(), valueAlpha);
        var hex2 = Color.parseHex(obj.get("hex2").getAsString(), otherValueAlpha);
        hex1.ifPresent(value::setToColor);
        hex2.ifPresent(otherValue::setToColor);
    }


    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("hex1", this.getValue().getHexCode(valueAlpha));
        obj.addProperty("hex2", this.getOtherValue().getHexCode(otherValueAlpha));
    }

    @Override
    public String getType() {
        return "dual_color";
    }
}
