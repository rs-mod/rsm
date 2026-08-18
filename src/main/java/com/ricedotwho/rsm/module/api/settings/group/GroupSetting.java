package com.ricedotwho.rsm.module.api.settings.group;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.settings.Setting;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;

public class GroupSetting<T extends SubModule<?>> extends Setting<T> {
    public GroupSetting(String name, T sub, BooleanSupplier supplier) {
        super(name, supplier, null, "", null);
        this.value = sub;
    }

    @SuppressWarnings("unused")
    public GroupSetting(String name, T sub) {
        super(name, null, null, "", null);
        this.value = sub;
    }

    public Setting<?> get(String setting) {
        for (Setting<?> s : this.value.getSettings()) {
            if (s.getName().equals(setting)) {
                return s;
            }
        }
        return null;
    }

    public void add(ArrayList<Setting<?>> settings) {
        this.value.internalRegisterProperty(settings);
        for (Setting<?> setting : settings) {
            setting.attached = true;
        }
    }

    public void add(Setting<?>... settings) {
        this.value.internalRegisterProperty(settings);
        for (Setting<?> setting : settings) {
            setting.attached = true;
        }
    }

    @Override
    public void resetToDefault() {
        for (Setting<?> setting : this.value.getSettings()) {
            setting.resetToDefault();
        }
    }

    @Override
    public void readFromJson(JsonObject obj) {

    }

    @Override
    public void writeToJson(JsonObject obj) {

    }

    @Override
    public String getType() {
        return "group";
    }
}
