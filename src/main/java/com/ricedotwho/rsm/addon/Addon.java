package com.ricedotwho.rsm.addon;

import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.module.Module;

import java.util.List;

public interface Addon {

    void onInitialize();

    void onUnload();

    List<Class<? extends Module>> getModules();

    List<Class<? extends ModComponent>> getComponents();

    List<Class<? extends Command>> getCommands();
}
