package com.ricedotwho.rsm.ui.clickgui.settings.group;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;

import java.util.function.BooleanSupplier;

public class GroupSetting<T extends SubModule<?>> extends Setting<T> {

    public GroupSetting(String name, T sub, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.value = sub;
    }

    public GroupSetting(String name, T sub) {
        super(name, null, null);
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

    public void add(Setting<?>... settings) {

        this.value.registerProperty(settings);
        for (int i = 0; i < settings.length; i++) {
            if (settings[i] == null) {
                throw new IllegalStateException(
                        "Null Setting at index " + i + " passed to GroupSetting.add() in "
                                + this.value.getClass().getSimpleName()
                                + ". This is almost always caused by a static 'instance' field being "
                                + "declared BEFORE the static Setting fields it references in the constructor — "
                                + "check static field declaration order in that class."
                );
            }
            settings[i].attached = true;
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
