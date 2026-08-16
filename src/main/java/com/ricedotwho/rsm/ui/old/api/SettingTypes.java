package com.ricedotwho.rsm.ui.old.api;

import com.ricedotwho.rsm.ui.old.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.old.clickgui.settings.Setting;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class SettingTypes {
    private final Map<Class<? extends Setting<?>>, Class<? extends ValueComponent<?>>> SETTINGS = new HashMap<>();

    public void register(Class<? extends Setting<?>> setting, Class<? extends ValueComponent<? extends Setting<?>>> valueComponent) {
        SETTINGS.put(setting, valueComponent);
    }

    public static  Class<? extends ValueComponent<?>> getValueComponent(Class<? extends Setting> setting) {
        Class<? extends ValueComponent<?>> component = SETTINGS.get(setting);
        if (component == null) {
            // schizo fallback
            for (Map.Entry<Class<? extends Setting<?>>, Class<? extends ValueComponent<?>>> entry : SETTINGS.entrySet()) {
                Class<?> registered = entry.getKey();
                if (registered.isAssignableFrom(setting)) {
                    return entry.getValue();
                }
            }
//            RSM.getLogger().warn("Setting class \"{}\" has not been registered!", setting.getName());
//            return EmptyValueComponent.class;
            throw new IllegalArgumentException("Setting class \"" + setting.getName() +"\" has not been registered!");
        }
        return component;
    }
}
