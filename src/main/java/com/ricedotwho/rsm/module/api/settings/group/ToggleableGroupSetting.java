package com.ricedotwho.rsm.module.api.settings.group;

import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;

public class ToggleableGroupSetting extends GroupSetting<ToggleableGroupSetting.ToggleableSubModule> {

    @SuppressWarnings("unused")
    public ToggleableGroupSetting(String name, Module module) {
        super(name, new ToggleableSubModule(module, name));
    }

    @SubModuleInfo(name = "ToggleableSubModule", alwaysDisabled = false)
    public static class ToggleableSubModule extends SubModule<Module> {
        public ToggleableSubModule(Module module, String name) {
            super(module, name);
        }
    }
}
