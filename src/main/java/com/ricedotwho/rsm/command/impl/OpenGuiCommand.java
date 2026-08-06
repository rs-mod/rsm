package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "opengui", aliases = "o", description = "Opens the ClickGUI")
public class OpenGuiCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    Scheduler.schedule(ClientTickEvent.Start.class, () -> ClickGUI.getInstance().toggle());
                    return 1;
                });
    }
}
