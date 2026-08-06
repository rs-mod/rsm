package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ModuleManager {
    @Getter
    private final ArrayList<Module> modules = new ArrayList<>();

    @Init
    private void init() {
        addModules(GeneratedModuleList.INSTANCE.getModules());
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
            module.registerSettings();

            module.loadConfig();
            modules.add(module);
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

        var singletonInstance = ReflectionUtils.getSingleton(clazz);
        if (singletonInstance == null) throw new RuntimeException(name + " is not a singleton.");
        return (Module) singletonInstance;
    }

    public void saveModules() {
        for (Module module : modules) {
            module.saveConfig();
        }
    }

}
