package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "copy", description = "Copies text to clipboard")
public class CopyCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(argument("contents", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String contents = StringArgumentType.getString(ctx, "contents");
                            mc.keyboardHandler.setClipboard(contents);
                            return 1;
                        })
                );
    }
}
