package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "vw", description = "Customise Visual Words")
public class VisualWordCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(_ -> {
                    mc.execute(VisualWords::open);
                    return 1;
                })
                .then(literal("add")
                        .then(argument("phrase", StringArgumentType.word())
                                .then(argument("replacement", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String phrase = StringArgumentType.getString(ctx, "phrase");
                                            String replacement = StringArgumentType.getString(ctx, "replacement");
                                            VisualWords.addWord(phrase, replacement);
                                            ChatUtils.chat("Added replacement {} for {}", replacement, phrase);
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
                                        ChatUtils.chat("Removed {}", phrase);
                                    } else {
                                        ChatUtils.chat("No word with phrase \"{}\" was found!", phrase);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(_ -> {
                            VisualWords.getInstance().getData().getValue().forEach(ChatUtils::chat);
                            return 1;
                        })
                )
                .then(literal("load")
                        .executes(_ -> {
                            VisualWords.getInstance().getData().load();
                            ChatUtils.chat("Loaded!");
                            return 1;
                        })
                );
    }
}
