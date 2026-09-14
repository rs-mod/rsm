package com.ricedotwho.rsm.module.api.settings;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import manifold.ext.rt.api.Self;

import java.util.function.BooleanSupplier;

@Getter
public abstract class Setting<T> {
    private static final BooleanSupplier defaultVisible = () -> true;

    private final String name;
    private BooleanSupplier isVisible = defaultVisible;
    @Setter
    protected T value;
    protected T defaultValue;
    @Setter
    private boolean shown;
    private Runnable onEdit = null;
    private final String description;
    public boolean attached = false;

    @Setter
    private boolean notPersistent = false;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;

        this.shown = this.isVisible.getAsBoolean();
    }

    public final @Self Setting<T> isVisible(BooleanSupplier supplier) {
        this.isVisible = supplier;
        return this;
    }

    public final @Self Setting<T> onEdit(Runnable runnable) {
        this.onEdit = runnable;
        return this;
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
