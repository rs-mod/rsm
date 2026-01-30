package com.ricedotwho.rsm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class DataStore {
    private final Map<String, Object> data = new HashMap<>();

    public <T> void put(String key, T value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T computeIfAbsent(String key, Function<? super String, ? extends T> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        T v;
        if ((v = get(key)) == null) {
            T newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return v;
    }
}