package com.ricedotwho.rsm.module.api.settings.group;

import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;

import java.util.function.BooleanSupplier;

public class DefaultGroupSetting extends GroupSetting<DefaultGroupSetting.DefaultSubModule> {

    @SuppressWarnings("unused")
    public DefaultGroupSetting(String name, Module module, BooleanSupplier supplier) {
        super(name, new DefaultSubModule(module, name), supplier);
    }

    public DefaultGroupSetting(String name, Module module) {
        super(name, new DefaultSubModule(module, name), null);
    }


    @SubModuleInfo(name = "DefaultSubModule")
    public static class DefaultSubModule extends SubModule<Module> {
        public DefaultSubModule(Module module, String name) {
            super(module, name);
        }
    }
}
