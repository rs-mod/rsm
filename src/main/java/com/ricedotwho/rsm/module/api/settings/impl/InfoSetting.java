package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.elements.SettingElementContainer;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class InfoSetting extends Setting<Void> {
    private static final Color TEXT = Color.fromHex(0xFFFFFF);
    private static final Color INFO = Color.fromHex(0x5A5A5A);
    private static final Color WARNING = Color.fromHex(0xBA8E23);

    private final Color colour;
    private final Color lineColour;

    public InfoSetting(String name, Color colour, Color lineColour, BooleanSupplier supplier) {
        super(name, supplier, null, "", null);
        this.colour = colour;
        this.lineColour = lineColour;
    }

    public InfoSetting(String name, Type type, BooleanSupplier supplier) {
        Color lineColour;
        switch (type) {
            case INFO -> lineColour = INFO;
            case WARNING -> lineColour = WARNING;
            default -> lineColour = SettingElementContainer.elementStrokeColor;
        }

        this(name, TEXT, lineColour, supplier);
    }

    public InfoSetting(String name, Type type) {
        this(name, type, null);
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
        return "info";
    }

    @Override
    public boolean savesToConfig() {
        return false;
    }

    public enum Type {
        WARNING,
        INFO
    }
}
