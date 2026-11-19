package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.command.arguments.StringCompletingArgument;
import com.ricedotwho.rsm.managers.dungeon.map.DungeonScanner;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.DungeonWaypoint;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.List;
import java.util.Set;

@CommandInfo(name = "ether", aliases = "ew", description = "Ignore rooms for camera zpew")
public class EtherCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .then(argument("room", new RoomArg())
                                .executes(ctx -> {
                                    String room = RoomArg.get(ctx, "room");
                                    Ether.getInstance().getIgnoredRooms().getValue().add(room);
                                    Ether.getInstance().getIgnoredRooms().save();
                                    ChatUtils.chat("Added \"{}\"", room);
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("room", new RoomArg())
                                .executes(ctx -> {
                                    String room = RoomArg.get(ctx, "room");
                                    Ether.getInstance().getIgnoredRooms().getValue().remove(room);
                                    Ether.getInstance().getIgnoredRooms().save();
                                    ChatUtils.chat("Removed \"{}\"", room);
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(_ -> {
                            Set<String> list = Ether.getInstance().getIgnoredRooms().getValue();
                            ChatUtils.chat("Rooms: {}", list);
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(_ -> {
                            DungeonWaypoint.clear();
                            ChatUtils.chat("Cleared");
                            return 1;
                        })
                );
    }

    private static class RoomArg extends StringCompletingArgument {
        public RoomArg() {
            super(DungeonScanner::getROOM_NAMES, List.of("Atlas", "Altar"), true);
        }
    }
}