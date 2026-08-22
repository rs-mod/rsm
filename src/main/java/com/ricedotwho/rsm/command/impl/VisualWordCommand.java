package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.ui.old.visualwords.VisualWordGui;
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
                .then(literal("open")
                        .executes(ctx -> {
                            Scheduler.schedule(TickEvent.ClientStart.class, VisualWordGui::open);
                            return 1;
                        })
                )
                .then(literal("gui")
                        .executes(ctx -> {
                            Scheduler.schedule(TickEvent.ClientStart.class, VisualWordGui::open);
                            return 1;
                        })
                )
                .then(literal("list")
                        .executes(ctx -> {
                            VisualWords.getInstance().getData().getValue().keySet().forEach(ChatUtils::chat);
                            return 1;
                        })
                )
                .then(literal("load")
                        .executes(ctx -> {
                            VisualWords.getInstance().getData().load();
                            ChatUtils.chat("Loaded!");
                            return 1;
                        })
                );
    }
}
