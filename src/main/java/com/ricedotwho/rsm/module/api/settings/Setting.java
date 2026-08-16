package com.ricedotwho.rsm.module.api.settings;



import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
public abstract class Setting<T> {
    private final String name;
    @Getter
    private final BooleanSupplier isVisible;
    @Getter
    @Setter
    protected T value;
    @Getter
    protected T defaultValue;
    @Setter
    private boolean shown;
    @Getter
    private final Runnable onEdit;

    @Getter
    private final String description;

    public boolean attached = false;

    @Getter
    @Setter
    private boolean notPersistent = false;

    public Setting(String name, BooleanSupplier isVisible, Runnable onEdit, String description, T defaultValue) {
        this.name = name;
        this.isVisible = (isVisible != null) ? isVisible : () -> true;
        this.shown = this.isVisible.getAsBoolean();
        this.onEdit = onEdit;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public abstract void readFromJson(JsonObject obj);

    public abstract void writeToJson(JsonObject obj);

    public abstract String getType();

    public boolean savesToConfig() {
        return true;
    }

    public void resetToDefault() {
        value = defaultValue;
    }

    public void onEdit() {
        if (this.onEdit != null) this.onEdit.run();
    }
}
