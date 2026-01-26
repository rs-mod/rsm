package com.ricedotwho.rsm.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
public class Manager<T> {
    private final HashMap<Class<?>, T> map;

    public Manager() {
        map = new HashMap<>();
    }

    public void put(T obj) {
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
        map.remove(obj.getClass());
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

    public T get(Class clazz) {
        return map.get(clazz);
    }

}
