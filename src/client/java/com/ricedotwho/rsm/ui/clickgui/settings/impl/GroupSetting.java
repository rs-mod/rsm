package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class GroupSetting extends Setting<List<Setting>> {
    public GroupSetting(String name, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = new ArrayList<>();
    }

    public GroupSetting(String name) {
        super(name, null);
        this.value = new ArrayList<>();
    }

    public void add(Setting setting) {
        this.value.add(setting);
    }

    public Setting get(String setting) {
        for (Setting s : this.value) {
            if (s.getName().equals(setting)) {
                return s;
            }
        }
        return null;
    }

    public void add(Setting... settings) {
        this.value.addAll(Arrays.asList(settings));
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
