package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.ui.old.chathider.ChatHiderGui;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "chathider", aliases = "ch", description = "Opens the chat hider gui")
public class ChatHiderCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    Scheduler.schedule(TickEvent.ClientStart.class, ChatHiderGui::open);
                    return 1;
                });
    }
}
