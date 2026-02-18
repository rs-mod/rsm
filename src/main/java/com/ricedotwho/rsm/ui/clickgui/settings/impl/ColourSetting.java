package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
public class ColourSetting extends Setting<Colour> {
    @Setter
    private Colour value;
    private final Colour defaultValue;

    public ColourSetting(String name, Colour defaultValue, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColourSetting(String name, Colour defaultValue) {
        this(name, defaultValue, null);
    }

    public ColourSetting(String name, Color defaultValue) {
        this(name, new Colour(defaultValue), null);
    }

    public void resetToDefault() {
        this.value = defaultValue.copy();
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        short h = obj.get("hue").getAsShort();
        short s = obj.get("saturation").getAsShort();
        short b = obj.get("brightness").getAsShort();
        short a = obj.get("alpha").getAsShort();
        int dataBit = obj.get("dataBit").getAsInt();

        this.setValue(new Colour(h, s, b, a, dataBit));
    }
}
