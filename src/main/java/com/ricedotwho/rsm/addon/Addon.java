package com.ricedotwho.rsm.addon;

import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.type.Accessor;

import java.util.List;

public interface Addon extends Accessor {

    void onInitialize();

    void onUnload();

    List<Class<?>> getModules();

    List<Class<?>> getRegisteredClasses();

    List<Class<? extends Command>> getCommands();
}
