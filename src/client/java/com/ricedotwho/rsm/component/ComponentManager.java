package com.ricedotwho.rsm.component;

import java.util.HashMap;
import java.util.List;

public class ComponentManager {

    private final HashMap<Class<?>, Component> map;

    public ComponentManager() {
        map = new HashMap<>();
    }

    public void put(Component obj) {
        map.put(obj.getClass(), obj);
        obj.setEnabled(true);
    }

    public final void put(Component... objs) {
        for (Component obj : objs) {
            put(obj);
        }
    }

    public final void put(List<Component> objs) {
        for (Component obj : objs) {
            put(obj);
        }
    }

    public void remove(Component obj) {
        Component instance = map.remove(obj.getClass());
        instance.setEnabled(false);
    }

    public final void remove(Component... objs) {
        for (Component obj : objs) {
            remove(obj);
        }
    }

    public final void remove(List<Component> objs) {
        for (Component obj : objs) {
            remove(obj);
        }
    }

    public Component get(Class<? extends Component> clazz) {
        return map.get(clazz);
    }
}
