package com.ricedotwho.rsm.ui.clickgui.settings;


import com.google.gson.JsonObject;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
public abstract class Setting<T> {
    private boolean registered = false;
    private final String name;
    private final boolean shouldSubscribe;
    @Getter
    private final BooleanSupplier supplier;
    @Getter
    @Setter
    protected T value;
    @Getter
    protected T defaultValue;
    @Setter
    private boolean shown;
    @Getter
    private final Runnable onEdit;

    public Setting(String name, BooleanSupplier supplier, Runnable onEdit) {
        this.name = name;
        this.shouldSubscribe = supplier != null;
        this.supplier = (supplier != null) ? supplier : () -> true;
        this.shown = this.supplier.getAsBoolean();
        this.onEdit = onEdit;
    }

    public abstract void loadFromJson(JsonObject obj);

    public abstract void saveToJson(JsonObject obj);

    public abstract String getType();

    public boolean savesToConfig() {
        return true;
    }

    public void register() {
        if (!this.shouldSubscribe || registered) return;
        registered = true;
        RSM.getInstance().getEventBus().register(this);
    }

    public void unregister() {
        if (!this.shouldSubscribe || !registered) return;
        registered = false;
        RSM.getInstance().getEventBus().unregister(this);
    }

    public void onEdit() {
        if (this.onEdit != null) this.onEdit.run();
    }

    @SubscribeEvent
    public void onUpdateShown(Render2DEvent event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
