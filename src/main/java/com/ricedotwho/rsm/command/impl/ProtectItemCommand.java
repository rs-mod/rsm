package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.Scheduler;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.impl.player.ProtectItem;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "protectitem", aliases = "pi", description = "Protects your held item")
public class ProtectItemCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    if (mc.player != null) ProtectItem.addOrRemove(mc.player.getMainHandItem(), true);
                    return 1;
                });
    }
}
