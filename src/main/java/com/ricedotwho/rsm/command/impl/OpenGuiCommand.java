package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "opengui", aliases = "o", description = "Opens the ClickGUI")
public class OpenGuiCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    TaskComponent.onTick(0, () -> RSM.getModule(ClickGUI.class).toggle());
                    return 1;
                });
    }
}
