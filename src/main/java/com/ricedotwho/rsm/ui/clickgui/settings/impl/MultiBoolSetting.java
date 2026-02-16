package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Getter
public class MultiBoolSetting extends Setting<Map<String, Boolean>> {

    public MultiBoolSetting(String name, List<String> options, BooleanSupplier supplier) {
        super(name, supplier);
        Map<String, Boolean> values = new LinkedHashMap<>();
        for (String option : options) {
            values.put(option, false);
        }
        this.setValue(values);
    }

    public MultiBoolSetting(String name, List<String> options, List<String> enabledOptions) {
        super(name, null);
        Map<String, Boolean> values = new LinkedHashMap<>();
        for (String option : options) {
            values.put(option, enabledOptions.contains(option));
        }
        this.setValue(values);
    }

    public boolean get(String key) {
        return this.getValue().getOrDefault(key, false);
    }

    public void set(String key, boolean value) {
        if (this.getValue().containsKey(key)) {
            this.getValue().put(key, value);
        }
    }

    public void toggle(String key) {
        if (this.getValue().containsKey(key)) {
            this.getValue().put(key, !this.getValue().get(key));
        }
    }

    public String[] getValues() {
        List<String> enabled = new ArrayList<>();
        this.value.forEach((value, on) -> {
            if (on) enabled.add(value);
        });
        return enabled.toArray(new String[0]);
    }

    public List<String>  getValuesList() {
        List<String> enabled = new ArrayList<>();
        this.value.forEach((value, on) -> {
            if (on) enabled.add(value);
        });
        return enabled;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        JsonArray boolArray = obj.getAsJsonArray("values");
        for (JsonElement boolElement : boolArray) {
            JsonObject boolObj = boolElement.getAsJsonObject();
            String key = boolObj.get("name").getAsString();
            boolean value = boolObj.get("value").getAsBoolean();
            this.set(key, value);
        }
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @SubscribeEvent
    public void onUpdateShown(Render2DEvent event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
