package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.data.Manager;
import com.ricedotwho.rsm.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModuleManager extends Manager<Module> {

    public List<Module> getModulesForCategory(Category category) {
        List<Module> modules = new ArrayList<>();
        for(Module module: getMap().values()){
            if(module.getCategory().equals(category)){
                modules.add(module);
            }
        }
        return modules;
    }

    public Module getModuleFromName(String name){
        List<Module> modules = getMap().values().stream()
                .filter(module -> Objects.equals(module.getName(), name))
                .toList();

        return modules.isEmpty() ? null : modules.getFirst();
    }

    public Module getModuleFromID(String id){
        List<Module> modules = getMap().values().stream()
                .filter(module -> Objects.equals(module.getID(), id))
                .toList();

        return modules.isEmpty() ? null : modules.getFirst();
    }

    public List<Module> getEnabledModules() {
        return getMap().values()
                .stream().filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    public List<Module> getModules() {
        return new ArrayList<>(getMap().values());
    }

    @Override
    public void put(Module module) {
        Module m = getModuleFromID(module.getID());
        if (m == null || module.getInfo().isOverwrite())  {
            if (m != null) getMap().remove(m.getClass());
            getMap().put(module.getClass(), module);
        }
    }
}
