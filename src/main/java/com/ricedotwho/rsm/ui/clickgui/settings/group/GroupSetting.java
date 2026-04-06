package com.ricedotwho.rsm.ui.clickgui.settings.group;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import net.minecraft.client.resources.language.I18n;

import java.util.function.BooleanSupplier;

public class GroupSetting<T extends SubModule<?>> extends Setting<T> {
    @Getter
    private final boolean general;

    public GroupSetting(String name, T sub, boolean general, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.value = sub;
        this.general = general;
    }

    public GroupSetting(String name, T sub, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.value = sub;
        this.general = false;
    }

    public GroupSetting(String name, T sub) {
        super(name, null, null);
        this.value = sub;
        this.general = false;
    }

    public Setting<?> get(String setting) {
        for (Setting<?> s : this.value.getSettings()) {
            if (s.getName().equals(setting)) {
                return s;
            }
        }
        return null;
    }

    public void add(Setting<?>... settings) {
        this.value.registerProperty(settings);
    }

    @Override
    public void loadFromJson(JsonObject obj) {

    }

    @Override
    public void saveToJson(JsonObject obj) {

    }

    @Override
    public String getType() {
        return "group";
    }
}
