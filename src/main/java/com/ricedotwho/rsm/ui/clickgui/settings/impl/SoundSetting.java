package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.*;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Getter
@Setter
public class SoundSetting extends Setting<String> implements Accessor {
    private float pitch;
    private float volume;

    public SoundSetting(String name, String sound, float pitch, float volume, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        this.setValue(obj.get("value").getAsString());
        this.pitch = obj.get("pitch").getAsFloat();
        this.volume = obj.get("volume").getAsFloat();
    }

    @Override
    public void saveToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue());
        obj.addProperty("pitch", this.pitch);
        obj.addProperty("volume", this.volume);

    }

    @Override
    public String getType() {
        return "sound";
    }
}
