package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean value, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = value;
    }
    public BooleanSetting(String name, boolean value) {
        super(name, null);
        this.value = value;
    }

    public void toggle(){
        value = !value;
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
