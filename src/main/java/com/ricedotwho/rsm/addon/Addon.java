package com.ricedotwho.rsm.addon;

import com.ricedotwho.rsm.command.Command;

import java.util.List;

public interface Addon {

    void onInitialize();

    void onUnload();

    List<Class<?>> getModules();

    List<Class<?>> getRegisteredClasses();

    List<Class<? extends Command>> getCommands();
}
