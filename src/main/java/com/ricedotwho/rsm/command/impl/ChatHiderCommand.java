package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.player.Chat;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "chathider", aliases = "ch", description = "Opens the chat hider gui")
public class ChatHiderCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(_ -> {
                    mc.execute(Chat::open);
                    return 1;
                });
    }
}
