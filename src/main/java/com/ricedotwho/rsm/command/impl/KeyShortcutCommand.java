package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.Scheduler;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.ui.keyshortcuts.KeyShortcutGui;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "keyshortcuts", aliases = "ks", description = "Opens the key shortcuts gui")
public class KeyShortcutCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    Scheduler.schedule(ClientTickEvent.Start.class, KeyShortcutGui::open);
                    return 1;
                });
    }
}
