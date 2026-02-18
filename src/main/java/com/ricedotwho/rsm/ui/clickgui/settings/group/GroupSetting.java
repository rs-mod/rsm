package com.ricedotwho.rsm.ui.clickgui.settings.group;

import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;

import java.util.function.BooleanSupplier;

public class GroupSetting<T extends SubModule<?>> extends Setting<T> {

    public GroupSetting(String name, T sub, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = sub;
    }

    public GroupSetting(String name, T sub) {
        super(name, null);
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
    }
}
