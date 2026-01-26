package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;

@CommandInfo(aliases = { "editgui", "eg" }, description = "Opens the Gui Editor")
public class OpenGuiEditCommand extends Command {
    @Override
    public void execute(String[] args, String message) {
        TaskComponent.onTick(0, RSMGuiEditor::open);
    }
}
