package com.ricedotwho.rsm.ui.clickgui.settings.group;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;

import java.util.function.BooleanSupplier;

public class DefaultGroupSetting extends GroupSetting<DefaultGroupSetting.DefaultSubModule> {


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
