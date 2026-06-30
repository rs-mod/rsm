package com.ricedotwho.rsm.command.impl;


import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.LeapGui;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.Arrays;
import java.util.List;

@CommandInfo(name = "config", aliases = "c", description = "Manages client configurations")
public class LeapOrderCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("set")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .then(argument("player", StringArgumentType.string())
                                        .executes(ctx -> {
                                            int index = IntegerArgumentType.getInteger(ctx, "index");
                                            String player = StringArgumentType.getString(ctx, "player");
                                            LeapGui.getLeapOrder().getValue().set(index, player);
                                            LeapGui.getLeapOrder().save();
                                            ChatUtils.chat("Set index %s to %s", index, player);
                                            return 1;
                                        })
                                )
                        )
                        .then(argument("players", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String[] players = StringArgumentType.getString(ctx, "players").split(" ");
                                    List<String> leapOrder = LeapGui.getLeapOrder().getValue();
                                    leapOrder.clear();
                                    leapOrder.addAll(Arrays.asList(players));
                                    LeapGui.getLeapOrder().save();
                                    ChatUtils.chat("Added: %s", Arrays.toString(players));
                                    return 1;
                                })
                        )
                )
                .then(literal("get")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder();
                            for (String s : LeapGui.getLeapOrder().getValue()) {
                                sb.append(s).append(" ");
                            }
                            ChatUtils.chat("Leap order: %s", sb.toString().trim());
                            return 1;
                        })
                );
    }
}
