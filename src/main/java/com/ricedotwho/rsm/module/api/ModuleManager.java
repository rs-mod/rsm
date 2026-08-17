package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ModuleManager {
    @Getter
    private final ArrayList<Module> modules = new ArrayList<>();

    @Init
    private void init() {
        addModules(GeneratedModuleList.modules);
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

}
