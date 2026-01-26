package com.ricedotwho.rsm.addon;

import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.Component;

import java.util.List;

public interface Addon {
    void onLoad();

    void onUnload();

    String getName();

    List<Class<? extends Module>> getModules();

    List<Class<? extends Component>> getComponents();

    List<Class<? extends Command>> getCommands();
}
