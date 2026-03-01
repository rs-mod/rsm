package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.SbStatTracker;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.module.impl.render.Jesus;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.ChunkAccess;

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
                            VisualWords.wordMap.keySet().forEach(ChatUtils::chat);
                            return 1;
                        })
                )
                .then(literal("load")
                        .executes(ctx -> {
                            VisualWords.load();
                            ChatUtils.chat("Loaded!");
                            return 1;
                        })
                );
    }
}
