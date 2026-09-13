package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;

@Getter
public class SoundSetting extends Setting<String> {
    private BigDecimal pitch;
    private BigDecimal volume;

    public SoundSetting(String name, String sound, float pitch = 1f, float volume = 1f, String description = "") {
        super(name, description, sound);
        this.value = sound;
        this.pitch = new BigDecimal(pitch);
        this.volume = new BigDecimal(volume);
    }

    @Override
    public void readFromJson(JsonObject obj) {
        this.setValue(obj.get("value").getAsString());
        this.pitch = obj.get("pitch").getAsBigDecimal();
        this.volume = obj.get("volume").getAsBigDecimal();
    }

    public double getVolume() {
        return this.volume.doubleValue();
    }

    public double getPitch() {
        return this.pitch.doubleValue();
    }

    public void setPitch(double value) {
        this.pitch = new BigDecimal(value);
    }

    public void setVolume(double value) {
        this.volume = new BigDecimal(value);
    }

    public void play() {
        Optional<Holder.Reference<@NotNull SoundEvent>> event = BuiltInRegistries.SOUND_EVENT.get(Identifier.withDefaultNamespace(this.value));
        if (event.isEmpty()) return;
        PlayerUtils.playSound(event.get().value(), this.pitch.floatValue(), this.volume.floatValue());
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
