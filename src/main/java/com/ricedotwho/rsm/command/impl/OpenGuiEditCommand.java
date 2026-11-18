package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.ui.impl.RSMGuiEditor;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "editgui", aliases = "eg", description = "Opens the Gui Editor")
public class OpenGuiEditCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(_ -> {
                    mc.execute(RSMGuiEditor::open);
                    return 1;
                });
    }
}
