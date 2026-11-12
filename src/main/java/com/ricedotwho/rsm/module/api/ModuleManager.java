package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleManager implements Accessor {
    @Getter
    private final ArrayList<Module> modules = new ArrayList<>();
    private final ArrayList<ModuleBase> registered = new ArrayList<>();
    @Getter
    private final ArrayList<HudSetting> hudSettings = new ArrayList<>();

    public void init(List<Class<?>> modules) {
        addModules(modules);
        EventBus.register(this);
    }

    public void removeModules(List<Class<?>> classes) {
        val moduleList = new ArrayList<Module>();

        for (Class<?> clazz : classes) {
            val instance = getModuleInstance(clazz);
            moduleList.add(instance);
        }
        modules.removeIf(moduleList::contains);
        for (Module module : moduleList) {
            module.saveConfig();
            module.getKeybind().unregister();
            module.setEnabled(false);
        }
    }

    public void addModules(List<Class<?>> classes) {
        val moduleList = new ArrayList<Module>();
        for (Class<?> clazz : classes) {
            val instance = getModuleInstance(clazz);
            moduleList.add(instance);
        }
        for (Module module : moduleList) {
            module.loadDefaults();
            module.registerFields();
            module.loadConfig();
            val possibleParent = modules.stream().filter(parentModule -> parentModule.getClass().isAssignableFrom(module.getClass())).findFirst();

            modules.add(module);

            if (possibleParent.isEmpty()) continue;

            val parent = possibleParent.orElseThrow();
            parent.setEnabled(false);


            val potentialInstanceField = ReflectionUtils.getSingletonField(parent.getClass());
            if (potentialInstanceField.isEmpty()) {
                modules.remove(parent);
                continue;
            }
            val instanceField = potentialInstanceField.get();

            if (ReflectionUtils.isFinal(instanceField)) {
                throw new RuntimeException(
                        "Tried to override module: " + parent.getClass().getSimpleName()
                        + ", field (" + instanceField.getName() + ") cannot be non-final"
                );
            }

            try {
                instanceField.setAccessible(true);
                instanceField.set(parent, module);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "tried to set module: " + parent.getClass().getSimpleName()
                        + ", field: " + instanceField.getName()
                        + ", child: " + module.getClass().getSimpleName(),
                        e
                );
            }

            modules.remove(parent);
        }
    }

    public Module getModuleFromID(String id){
        List<Module> modules = getModules().stream()
                .filter(module -> Objects.equals(module.getID(), id))
                .toList();

        return modules.isEmpty() ? null : modules.getFirst();
    }

    private Module getModuleInstance(Class<?> clazz) throws RuntimeException {
        var name = clazz.getTypeName();
        if (!Module.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(name + " is not assignable from Module.");
        }

        var instance = ReflectionUtils.getSingleton(clazz);
        if (instance == null) {
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return (Module) instance;
    }

    public void saveModules() {
        for (Module module : modules) {
            module.saveConfig();
        }
    }

    public void onRegister(Module module) {
        registered.add(module);
        hudSettings.addAll(module.getHudSettings());
    }

    public void onRegister(SubModule<?> module) {
        registered.add(module);
        hudSettings.addAll(module.getHudSettings());
    }

    public void onUnregister(Module module) {
        registered.remove(module);
        hudSettings.removeAll(module.getHudSettings());
    }

    public void onUnregister(SubModule<?> module) {
        registered.remove(module);
        hudSettings.removeAll(module.getHudSettings());
    }

    public void onAdd(HudSetting setting) {
        hudSettings.add(setting);
    }

    public void onRemove(HudSetting setting) {
        hudSettings.removeIf(it -> Objects.equals(it.getName(), setting.getName()));
    }

    public void dumpHuds() {
        ChatUtils.chat("Huds: {}", hudSettings.map(Setting::getName).toList());
    }

    @SubscribeEvent
    private void render(Render2DEvent event) {
        if (mc.level == null || mc.player == null || mc.gui.screen() == RSM.getInstance().getGUIEditor()) return;

        hudSettings.forEach(it -> it.draw(event.getGfx(), false));
    }
}
