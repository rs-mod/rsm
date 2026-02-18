package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import net.minecraft.data.recipes.SpecialRecipeBuilder;

import java.util.function.BooleanSupplier;

public class KeybindSetting extends Setting<Keybind> {
    @Getter
    private final boolean persistent;

    public KeybindSetting(String name, Keybind key, Runnable action, boolean persistent, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = key;
        this.persistent = persistent;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, Runnable action, BooleanSupplier supplier) {
        super(name, supplier);
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key, Runnable action) {
        super(name, null);
        this.value = key;
        this.persistent = false;
        this.value.setRunnable(action);
    }

    public KeybindSetting(String name, Keybind key) {
        super(name, null);
        this.persistent = false;
        this.value = key;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        JsonElement keyObj = obj.get("value");
        String key = keyObj == null ? "key.keyboard.unknown" : keyObj.getAsString();
        this.value.setKeyBind(InputConstants.getKey(key));
    }

    @SubscribeEvent
    public void onUpdateShown(Render2DEvent event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
