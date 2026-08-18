package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class SoundSetting extends Setting<String> {
    private float pitch;
    private float volume;

    public SoundSetting(String name, String sound, float pitch, float volume, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, sound);
        this.value = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    public SoundSetting(String name, String sound, float pitch, float volume, BooleanSupplier supplier) {
        this(name, sound, pitch, volume, supplier, "");
    }

    @Override
    public void readFromJson(JsonObject obj) {
        this.setValue(obj.get("value").getAsString());
        this.pitch = obj.get("pitch").getAsFloat();
        this.volume = obj.get("volume").getAsFloat();
    }

    @Override
    public void writeToJson(JsonObject obj) {
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
