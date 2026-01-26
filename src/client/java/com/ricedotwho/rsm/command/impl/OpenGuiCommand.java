package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;

@CommandInfo(aliases = {"opengui", "o"}, description = "Opens the ClickGUI")
public class OpenGuiCommand extends Command {

    @Override
    public void execute(String[] args, String message) {
        TaskComponent.onTick(0, () -> {
            RSM.getModule(ClickGUI.class).toggle();
        });
    }
}
