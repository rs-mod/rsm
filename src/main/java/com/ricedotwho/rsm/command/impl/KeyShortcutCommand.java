package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.KeyShortcuts;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "keyshortcuts", aliases = "ks", description = "Opens the key shortcuts gui")
public class KeyShortcutCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    mc.execute(KeyShortcuts::open);
                    return 1;
                });
    }
}
