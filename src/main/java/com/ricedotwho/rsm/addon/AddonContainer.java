package com.ricedotwho.rsm.addon;

import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class AddonContainer {
    private final List<Class<?>> modules;
    private final List<Command> commands;
    private final List<Class<?>> registrationList;
    private final Addon addon;
    private final AddonClassLoader cl;
    private final AddonMeta meta;
    private final boolean hasMixin;

    public AddonContainer(Addon addon, AddonClassLoader cl, AddonMeta meta, boolean hasMixin) {
        this.addon = addon;
        this.cl = cl;
        this.meta = meta;
        this.hasMixin = hasMixin;
        this.modules = addon.getModules();
        this.commands = AddonLoader.instantiate(addon.getCommands());
        this.registrationList = addon.getRegisteredClasses();


    }

    public void load(boolean reload) {
        ModuleManager.addModules(this.modules);

        if (reload) ClickGui.refreshModules();
        RSM.getInstance().getCommandManager().put(this.commands);

        EventBus.registerClasses(this.registrationList);
        if (hasMixin) this.addon.onInitialize();
    }

    public void unLoad() {
        if (hasMixin) return;
        this.addon.onUnload();
        ModuleManager.removeModules(this.modules);
        ClickGui.refreshModules();
        RSM.getInstance().getCommandManager().remove(this.commands);


        EventBus.unregister(this.registrationList);


        try {
            cl.close();
        } catch (IOException e) {
            RSM.getLogger().warn("Failed to close addon classloader", e);
        }
    }
}
