package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final Class<T> enumClass;

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Class<T> deriveEnumClass(T defaultValue) {
        Class<?> raw = defaultValue.getClass();
        // walk up if it's an anonymous constant-body subclass
        if (!raw.isEnum()) raw = raw.getSuperclass();
        return (Class<T>) raw;
    }

    public EnumSetting(String name, T defaultValue, Runnable onEdit, BooleanSupplier supplier, String description) {
        super(name, supplier, onEdit, description, defaultValue);

        this.value = defaultValue;
        this.enumClass = deriveEnumClass(defaultValue);
    }

    public EnumSetting(String name, T defaultValue, BooleanSupplier supplier, String description) {
        super(name, supplier, null, description, defaultValue);
        this.value = defaultValue;
        this.enumClass = deriveEnumClass(defaultValue);
    }

    public EnumSetting(String name, T defaultValue, String description) {
        super(name, null, null, description, defaultValue);
        this.value = defaultValue;
        this.enumClass = deriveEnumClass(defaultValue);
    }

    public EnumSetting(String name, T defaultValue, Runnable onEdit, BooleanSupplier supplier) {
        super(name, supplier, onEdit, "", defaultValue);
        this.value = defaultValue;
        this.enumClass = deriveEnumClass(defaultValue);
    }

    public EnumSetting(String name, T defaultValue, BooleanSupplier supplier) {
        super(name, supplier, null, "", defaultValue);
        this.value = defaultValue;
        this.enumClass = deriveEnumClass(defaultValue);
    }

    public EnumSetting(String name, T defaultValue) {
        super(name, null, null, "", defaultValue);
        this.value = defaultValue;
        this.enumClass = deriveEnumClass(defaultValue);
    }

    public boolean is(T other) {
        return this.value == other;
    }

    public void setByIndex(int index) {
        T[] values = getValues();
        if (values.length <= index) return;
        this.value = values[index];
    }

    public int getIndex() {
        return this.value.ordinal();
    }

    public boolean inRange(int min, int max) {
        int index = getIndex();
        return min < index && max > index;
    }

    public boolean inRangeInclusive(int min, int max) {
        int index = getIndex();
        return min <= index && max >= index;
    }

    public String[] getDisplayOptions() {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(EnumSetting::toDisplayName)
                .toArray(String[]::new);
    }

    private static String toDisplayName(Enum<?> value) {
        String[] words = value.name().toLowerCase().split("_");
        return Arrays.stream(words)
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Override
    public void readFromJson(JsonObject obj) {
        this.value = Enum.valueOf(enumClass, obj.get("value").getAsString());
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("value", this.getValue().name());
    }

    public T[] getValues() {
        return enumClass.getEnumConstants();
    }

    @Override
    public String getType() {
        return "enum";
    }
}
