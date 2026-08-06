package com.ricedotwho.rsm.ui.clickgui.settings;


import com.google.gson.JsonObject;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private boolean registered = false;
    private final boolean shouldSubscribe;

    @Getter
    private final String name;

    @Getter
    private final BooleanSupplier supplier;
    @Getter
    @Setter
    protected T value;
    @Getter
    protected T defaultValue;

    @Setter
    @Getter
    private boolean shown;
    @Getter
    private final Runnable onEdit;

    @Getter
    @Setter
    private boolean notPersistent = false;

    @Getter
    public boolean attached = false;

    public Setting(String name, BooleanSupplier supplier, Runnable onEdit) {
        this.name = name;
        this.shouldSubscribe = supplier != null;
        this.supplier = (supplier != null) ? supplier : () -> true;
        this.shown = this.supplier.getAsBoolean();
        this.onEdit = onEdit;
    }

    public abstract void readFromJson(JsonObject obj);

    public abstract void writeToJson(JsonObject obj);

    public abstract String getType();

    public boolean doesNotSaveToConfig() {
        return false;
    }

    public void register() {
        if (!this.shouldSubscribe || registered) return;
        registered = true;
        EventBus.register(this);
    }

    public void unregister() {
        if (!this.shouldSubscribe || !registered) return;
        registered = false;
        EventBus.unregister(this);
    }

    public void onEdit() {
        if (this.onEdit != null) this.onEdit.run();
    }

    @SubscribeEvent
    private void onUpdateShown(Render2DEvent event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
