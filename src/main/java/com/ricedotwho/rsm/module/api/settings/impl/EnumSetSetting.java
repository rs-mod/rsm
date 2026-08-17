package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class EnumSetSetting<T extends Enum<T>> extends Setting<EnumSet<T>> {
    @Getter
    private final Class<T> enumClass;
    public EnumSetSetting(String name, Class<T> enumClass, @Nullable List<T> defaultValue, Runnable onEdit, BooleanSupplier isVisible, String description) {
        val defaultEnumSet = EnumSet.noneOf(enumClass);
        if (defaultValue != null) defaultEnumSet.addAll(defaultValue);
        super(name, isVisible, onEdit, description, defaultEnumSet);
        this.enumClass = enumClass;
        this.value = defaultEnumSet.clone();
    }

    public EnumSetSetting(String name, Class<T> enumClass, @NotNull List<T> defaultValue, BooleanSupplier isVisible, String description) {
        this(name, enumClass, defaultValue, null, isVisible, description);
    }

    public EnumSetSetting(String name, Class<T> enumClass, BooleanSupplier isVisible, String description) {
        this(name, enumClass, null, null, isVisible, description);
    }

    public EnumSetSetting(String name, Class<T> enumClass, @NotNull List<T> defaultValue, String description) {
        this(name, enumClass, defaultValue, null, () -> true, description);
    }

    public EnumSetSetting(String name, Class<T> enumClass, @NotNull List<T> defaultValue, Runnable onEdit, BooleanSupplier isVisible) {
        this(name, enumClass, defaultValue, onEdit, isVisible, "");
    }

    public EnumSetSetting(String name, Class<T> enumClass, @NotNull List<T> defaultValue, BooleanSupplier isVisible) {
        this(name, enumClass, defaultValue, null, isVisible, "");
    }

    public EnumSetSetting(String name, Class<T> enumClass, BooleanSupplier isVisible) {
        this(name, enumClass, null, null, isVisible, "");
    }

    public EnumSetSetting(String name, Class<T> enumClass, @NotNull List<T> defaultValue) {
        this(name, enumClass, defaultValue, null, () -> true, "");
    }

    public String[] getDisplayOptions() {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(EnumSetSetting::toDisplayName)
                .toArray(String[]::new);
    }

    private static String toDisplayName(Enum<?> value) {
        String[] words = value.name().toLowerCase().split("_");
        return Arrays.stream(words)
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    public final void remove(T option) {
        value.remove(option);
    }

    public final void add(T option) {
        value.add(option);
    }

    public final void add(String option) {
        getConstantFromString(option).ifPresent(constant -> value.add(constant));
    }

    public final void remove(String option) {
        getConstantFromString(option).ifPresent(constant -> value.remove(constant));
    }

    public final boolean contains(String option) {
        val constant = getConstantFromString(option);
        return constant.filter(t -> value.contains(t)).isPresent();
    }

    public final boolean contains(T option) {
        return value.contains(option);
    }

    @SafeVarargs
    public final boolean all(T... options) {
        for (T option : options) {
            if (!value.contains(option)) return false;
        }
        return true;
    }

    @SafeVarargs
    public final boolean any(T... options) {
        for (T option : options) {
            if (value.contains(option)) return true;
        }
        return false;
    }

    @SafeVarargs
    public final boolean none(T... options) {
        for (T option : options) {
            if (value.contains(option)) return false;
        }
        return true;
    }

    @Override
    public void readFromJson(JsonObject obj) {
        JsonArray enumArray = obj.getAsJsonArray("values");
        val values = EnumSet.noneOf(enumClass);
        for (JsonElement enumOption : enumArray) {
            val value = getConstantFromString(enumOption.getAsString());
            if (value.isEmpty()) continue;
            values.add(value.get());
        }
        this.value = values;
    }

    private Optional<T> getConstantFromString(String string) {
        return Arrays.stream(enumClass.getEnumConstants()).filter(constant -> constant.name().equals(string)).findAny();
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        JsonArray enumArray = new JsonArray();

        for (T key : value.stream().toList()) {
            enumArray.add(key.name());
        }
        obj.add("values", enumArray);
    }

    @Override
    public String getType() {
        return "multienum";
    }
}
