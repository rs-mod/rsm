package com.ricedotwho.rsm.module.api.settings.impl;

import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Color;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.function.BooleanSupplier;

@Getter
@SuppressWarnings("unused")
public class ColorSetting extends Setting<Color> {
    @Setter
    private Color value;
    private final Color defaultValue;

    public ColorSetting(String name, Color defaultValue, Runnable onEdit, BooleanSupplier supplier, String description) {
        super(name, supplier, onEdit, description, defaultValue);
        this.value = defaultValue.clone();
        this.defaultValue = defaultValue.clone();
    }

    public ColorSetting(String name, Color defaultValue, BooleanSupplier supplier, String description) {
        this(name, defaultValue, null, supplier, description);
    }

    public ColorSetting(String name, Color defaultValue, String description) {
        this(name, defaultValue, null, description);
    }

    public ColorSetting(String name, Color defaultValue, Runnable onEdit, BooleanSupplier supplier) {
        this(name, defaultValue, onEdit, supplier, "");
    }

    public ColorSetting(String name, Color defaultValue, BooleanSupplier supplier) {
        this(name, defaultValue, null, supplier, "");
    }

    public ColorSetting(String name, Color defaultValue) {
        this(name, defaultValue, null, "");
    }

    public ColorSetting(String name, int defaultValue, Runnable onEdit, BooleanSupplier supplier, String description) {
        val defaultColor = Color.WHITE.clone();
        defaultColor.setToColor(defaultValue);
        super(name, supplier, onEdit, description, defaultColor);
        this.value = defaultColor.clone();
        this.defaultValue = defaultColor;
    }

    public ColorSetting(String name, int defaultValue, BooleanSupplier supplier, String description) {
        this(name, defaultValue, null, supplier, description);
    }

    public ColorSetting(String name, int defaultValue, String description) {
        this(name, defaultValue, null, description);
    }

    public ColorSetting(String name, int defaultValue, Runnable onEdit, BooleanSupplier supplier) {
        this(name, defaultValue, onEdit, supplier, "");
    }

    public ColorSetting(String name, int defaultValue, BooleanSupplier supplier) {
        this(name, defaultValue, null, supplier, "");
    }

    public ColorSetting(String name, int defaultValue) {
        this(name, defaultValue, null, "");
    }

    @Override
    public void resetToDefault() {
        this.value.setToColor(defaultValue);
    }

    @Override
    public void readFromJson(JsonObject obj) {
        val potentialARGB = Color.parseHex(obj.get("hex").getAsString(), true);
        potentialARGB.ifPresent(value::setToColor);
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
