package com.ricedotwho.rsm.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Manager<T> {
    private final HashMap<Class<?>, T> map;

    public Manager() {
        map = new HashMap<>();
    }

    public void put(T obj) {
        if (map.containsKey(obj.getClass())) return;
        map.put(obj.getClass(), obj);
    }

    @SafeVarargs
    public final void put(T... objs) {
        for (T obj : objs) {
            put(obj);
        }
    }

    public final void put(List<T> objs) {
        for (T obj : objs) {
            put(obj);
        }
    }

    public void remove(T obj) {
        Class<?> clazz = obj.getClass();
        if (map.remove(clazz) == null) {
            for (Map.Entry<Class<?>, T> entry : map.entrySet()) {
                Class<?> previous = entry.getKey();
                if (clazz.isAssignableFrom(previous)) {
                    map.remove(previous);
                    return;
                }
            }
        }
    }

    @SafeVarargs
    public final void remove(T... objs) {
        for (T obj : objs) {
            remove(obj);
        }
    }

    public final void remove(List<T> objs) {
        for (T obj : objs) {
            remove(obj);
        }
    }

    public T getExact(Class<? extends T> clazz) {
        return map.get(clazz);
    }

    public T get(Class<? extends T> clazz) {

        T exact = map.get(clazz);
        if (exact != null) {
            return exact;
        }

        for (Map.Entry<Class<?>, T> entry : map.entrySet()) {
            Class<?> previous = entry.getKey();
            if (clazz.isAssignableFrom(previous)) {
                return entry.getValue();
            }
        }

        return null;
    }

}
