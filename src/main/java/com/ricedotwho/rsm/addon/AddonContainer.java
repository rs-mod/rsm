package com.ricedotwho.rsm.addon;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.ConfigUtils;
import lombok.Getter;
import com.ricedotwho.rsm.module.Module;

import java.io.IOException;
import java.util.List;

@Getter
public class AddonContainer {
    private final List<Module> modules;
    private final List<Command> commands;
    private final List<ModComponent> components;
    private final Addon addon;
    private final AddonClassLoader cl;
    private final AddonMeta meta;
    private final boolean hasMixin;

    public AddonContainer(Addon addon, AddonClassLoader cl, AddonMeta meta, boolean hasMixin) {
        this.addon = addon;
        this.cl = cl;
        this.meta = meta;
        this.hasMixin = hasMixin;
        this.modules = AddonLoader.instantiate(addon.getModules());
        this.commands = AddonLoader.instantiate(addon.getCommands());
        this.components = AddonLoader.instantiate(addon.getComponents());
    }

    public void load(boolean reload) {
        RSM.getInstance().getModuleManager().put(this.modules);
        this.modules.forEach(ConfigUtils::loadConfig);
//        if (RSM.getInstance().getConfigGui() != null && reload) RSM.getInstance().getConfigGui().reloadModules();
        RSM.getInstance().getCommandManager().put(this.commands);
        RSM.getInstance().getComponentManager().put(this.components);
        this.addon.onInitialize();
    }

    public void unLoad() {
        if (hasMixin) return;
        this.addon.onUnload();
        RSM.getInstance().getModuleManager().remove(this.modules);
        RSM.getInstance().getConfigGui().reloadModules();
        //todo: remove modules, use a different command system or fix somehow
        RSM.getInstance().getCommandManager().remove(this.commands);
        RSM.getInstance().getComponentManager().remove(this.components);

        // surely we aren't missing anything... this config is not built for this :sob:
        this.modules.forEach(m -> {
            ConfigUtils.saveConfig(m);
            m.getKeybind().unregister();
            m.setEnabled(false);
            m.getSettings().forEach(s -> {
                s.unregister();
                if (s instanceof KeybindSetting) {
                    ((KeybindSetting) s).getValue().unregister();
                }
            });
        });

        try {
            cl.close();
        } catch (IOException e) {
            RSM.getLogger().warn("Failed to close addon classloader", e);
        }
    }
}
