package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class ButtonSetting extends Setting<String> {
    private final Runnable action;

    public ButtonSetting(String name, String defaultValue, BooleanSupplier supplier, Runnable action) {
        super(name, supplier);
        this.value = defaultValue;
        this.defaultValue = value;
        this.action = action;
    }

    public ButtonSetting(String name, String defaultValue, Runnable action) {
        this(name, defaultValue, null, action);
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
