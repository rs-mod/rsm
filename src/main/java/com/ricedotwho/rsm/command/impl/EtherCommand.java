package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.DungeonWaypoint;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandInfo(name = "ether", aliases = "ew", description = "Ignore rooms for zpew")
public class EtherCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .then(argument("room", new RoomArgumentType())
                                .executes(ctx -> {
                                    String room = RoomArgumentType.getRoom(ctx, "room");
                                    Ether.getIgnoredRooms().getValue().add(room);
                                    Ether.getIgnoredRooms().save();
                                    ChatUtils.chat("Added \"%s\"", room);
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("room", new RoomArgumentType())
                                .executes(ctx -> {
                                    String room = RoomArgumentType.getRoom(ctx, "room");
                                    Ether.getIgnoredRooms().getValue().remove(room);
                                    Ether.getIgnoredRooms().save();
                                    ChatUtils.chat("Removed \"%s\"", room);
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                            Set<String> list = Ether.getIgnoredRooms().getValue();
                            ChatUtils.chat("Rooms: %s", list);
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(ctx -> {
                            DungeonWaypoint.clear();
                            ChatUtils.chat("Cleared");
                            return 1;
                        })
                );
    }

    private static class RoomArgumentType implements ArgumentType<String> {
        private static final Collection<String> EXAMPLES = List.of("Atlas", "Altar");
        private static final Set<String> VALUES = ScanUtils.getRoomNames();
        private static final DynamicCommandExceptionType INVALID_ROOM_EXCEPTION = new DynamicCommandExceptionType(
                room -> Component.literal("Invalid room type : " + room)
        );

        public String parse(StringReader stringReader) throws CommandSyntaxException {
            final String string = stringReader.getRemaining();
            stringReader.setCursor(stringReader.getTotalLength());
            if (!VALUES.contains(string)) {
                throw INVALID_ROOM_EXCEPTION.createWithContext(stringReader, string);
            } else {
                return string;
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider
                    ? SharedSuggestionProvider.suggest(VALUES, builder)
                    : Suggestions.empty();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static String getRoom(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, String.class);
        }
    }
}