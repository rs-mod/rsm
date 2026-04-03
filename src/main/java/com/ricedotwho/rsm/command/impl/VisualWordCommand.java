package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

@CommandInfo(name = "vw", description = "Customise Visual Words")
public class VisualWordCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .then(argument("phrase", StringArgumentType.word())
                                .then(argument("replacement", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String phrase = StringArgumentType.getString(ctx, "phrase");
                                            String replacement = StringArgumentType.getString(ctx, "replacement");
                                            VisualWords.addWord(phrase, Component.literal(replacement));
                                            ChatUtils.chat("Added replacement %s for %s", replacement, phrase);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("phrase", StringArgumentType.string())
                                .executes(ctx -> {
                                    String phrase = StringArgumentType.getString(ctx, "phrase");
                                    if (VisualWords.removeWord(phrase)) {
                                        ChatUtils.chat("Removed %s", phrase);
                                    } else {
                                        ChatUtils.chat("No word with phrase \"%s\" was found!", phrase);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                            VisualWords.getData().getValue().keySet().forEach(ChatUtils::chat);
                            return 1;
                        })
                )
                .then(literal("load")
                        .executes(ctx -> {
                            VisualWords.getData().load();
                            ChatUtils.chat("Loaded!");
                            return 1;
                        })
                );
    }
}
