package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "editgui", aliases = "eg", description = "Opens the Gui Editor")
public class OpenGuiEditCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    TaskComponent.onTick(0, RSMGuiEditor::open);
                    return 1;
                });
    }
}
