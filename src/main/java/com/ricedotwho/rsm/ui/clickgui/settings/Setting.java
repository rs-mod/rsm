package com.ricedotwho.rsm.ui.clickgui.settings;


import com.google.gson.JsonObject;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
public class Setting<T> {
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

    public Setting(String name, BooleanSupplier supplier) {
        this.name = name;
        this.shouldSubscribe = supplier != null;
        this.supplier = (supplier != null) ? supplier : () -> true;
        this.shown = this.supplier.getAsBoolean();
    }

    public void loadFromJson(JsonObject obj) {

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

    @SubscribeEvent
    public void onUpdateShown(Render2DEvent event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
