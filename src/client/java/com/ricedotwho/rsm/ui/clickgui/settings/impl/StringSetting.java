package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;
@Getter
public class StringSetting extends Setting<String> {
    private final boolean allowBlank;
    private final boolean secure;
    private final int maxLength;

    public StringSetting(String name,String defaultValue, BooleanSupplier supplier, boolean allowBlank, boolean secure, int maxLength) {
        super(name, supplier);
        this.value = defaultValue;
        this.defaultValue = value;
        this.allowBlank = allowBlank;
        this.secure = secure;
        this.maxLength = maxLength;
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank, boolean secure) {
        this(name, defaultValue, null, allowBlank, secure, 32);
    }

    public StringSetting(String name, String defaultValue, boolean allowBlank) {
        this(name, defaultValue, null, allowBlank, false, 32);
    }

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, null, true, false, 32);
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
