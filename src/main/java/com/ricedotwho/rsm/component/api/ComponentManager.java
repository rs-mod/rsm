package com.ricedotwho.rsm.component.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentManager {

    private final HashMap<Class<?>, ModComponent> map;

    public ComponentManager() {
        map = new HashMap<>();
    }

    public void put(ModComponent obj) {
        map.put(obj.getClass(), obj);
        obj.setEnabled(true);
    }

    public final void put(ModComponent... objs) {
        for (ModComponent obj : objs) {
            put(obj);
        }
    }

    public final void put(List<ModComponent> objs) {
        for (ModComponent obj : objs) {
            put(obj);
        }
    }

    public void remove(ModComponent obj) {
        ModComponent instance = map.remove(obj.getClass());
        instance.setEnabled(false);
    }

    public final void remove(ModComponent... objs) {
        for (ModComponent obj : objs) {
            remove(obj);
        }
    }

    public final void remove(List<ModComponent> objs) {
        for (ModComponent obj : objs) {
            remove(obj);
        }
    }

    public ModComponent getExact(Class<? extends ModComponent> clazz) {
        return map.get(clazz);
    }

    public ModComponent get(Class<? extends ModComponent> clazz) {

        ModComponent exact = map.get(clazz);
        if (exact != null) {
            return exact;
        }

        for (Map.Entry<Class<?>, ModComponent> entry : map.entrySet()) {

            Class<?> storedType = entry.getKey();

            if (clazz.isAssignableFrom(storedType)) {
                return entry.getValue();
            }
        }

        return null;
    }
}
