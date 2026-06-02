package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.dungeon.posmsg.PosMsg;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.DungeonWaypoint;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.Secret;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.EnumUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandInfo(name = "dwp", description = "Add or remove Dungeon Waypoint")
public class DungeonWaypointCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .then(argument("type", SecretArgumentType.secretArgumentType())
                                .executes(this::create)
                        )
                )
                .then(literal("remove")
                        .then(argument("type", SecretArgumentType.secretArgumentType())
                                .executes(ctx -> {
                                    SecretType type = SecretArgumentType.getSecretType(ctx, "type");
                                    if (DungeonWaypoint.removeClosest(type)) {
                                        ChatUtils.chat("Removed \"%s\"", type.name().toLowerCase());
                                    } else {
                                        ChatUtils.chat("No message found for \"%s\"", type.name().toLowerCase());
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("shift")
                        .then(argument("type", SecretArgumentType.secretArgumentType())
                                .then(argument("direction", DirectionArgumentType.directionArgumentType())
                                        .executes(ctx -> {
                                            SecretType type = SecretArgumentType.getSecretType(ctx, "type");
                                            Direction dir = DirectionArgumentType.getDirection(ctx, "direction");
                                            if (DungeonWaypoint.shiftClosest(type, dir, 1)) {
                                                ChatUtils.chat("Shifted %s in %s by %s", type.name().toLowerCase(), dir.getName().toLowerCase(), 1.0);
                                            } else {
                                                ChatUtils.chat( "%sFailed to shift %s!", ChatFormatting.RED, type);
                                            }
                                            return 1;
                                        })
                                        .then(argument("amount", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> {
                                                    SecretType type = SecretArgumentType.getSecretType(ctx, "type");
                                                    Direction dir = DirectionArgumentType.getDirection(ctx, "direction");
                                                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                    if (DungeonWaypoint.shiftClosest(type, dir, amount)) {
                                                        ChatUtils.chat("Shifted %s in %s by %s", type.name().toLowerCase(), dir.getName().toLowerCase(), amount);
                                                    } else {
                                                        ChatUtils.chat( "%sFailed to shift %s!", ChatFormatting.RED, type);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                            PosMsg.getMsgs().forEach(msg -> ChatUtils.chat("\"%s\" at %s", msg.message, msg.lower.toChatString()));
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(ctx -> {
                            PosMsg.clear();
                            ChatUtils.chat("Cleared");
                            return 1;
                        })
                );
    }

    private int create(CommandContext<ClientSuggestionProvider> ctx) {
        SecretType type = SecretArgumentType.getSecretType(ctx, "type");

        if (!(mc.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            ChatUtils.chat(ChatFormatting.RED + "Not looking at a block");
            return 0;
        }

        Pos pos = new Pos(blockHitResult.getBlockPos());
        Pos relPos = RoomUtils.getRelativePositionFixed(pos, Map.getCurrentRoom().getUniqueRoom().getMainRoom());

        Secret secret = new Secret(relPos, type);

        if (DungeonWaypoint.add(secret)) {
            ChatUtils.chat("Added %s at %s (%s)", secret.getType().name().toLowerCase(), secret.getTranslated().toChatString(), secret.getPos().toChatString());
        } else {
            ChatUtils.chat("Failed to add waypoint");
        }
        return 1;
    }


    private static class SecretArgumentType implements com.mojang.brigadier.arguments.ArgumentType<SecretType> {
        private static final Collection<String> EXAMPLES = Stream.of(SecretType.CHEST, SecretType.ESSENCE)
                .map(s -> s.name().toLowerCase())
                .collect(Collectors.toList());
        private static final SecretType[] VALUES = SecretType.values();
        private static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType(
                type -> Component.literal("Invalid secret type : " + type)
        );

        public SecretType parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readUnquotedString();
            SecretType secretType = EnumUtils.getEnum(SecretType.class, string.toUpperCase());
            if (secretType == null) {
                throw INVALID_TYPE_EXCEPTION.createWithContext(stringReader, string);
            } else {
                return secretType;
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider
                    ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(s -> s.name().toLowerCase()), builder)
                    : Suggestions.empty();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static SecretArgumentType secretArgumentType() {
            return new SecretArgumentType();
        }

        public static SecretType getSecretType(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, SecretType.class);
        }
    }

    private static class DirectionArgumentType implements ArgumentType<Direction> {
        private static final Collection<String> EXAMPLES = Stream.of(Direction.NORTH, Direction.WEST)
                .map(s -> s.name().toLowerCase())
                .collect(Collectors.toList());
        private static final Direction[] VALUES = Direction.values();
        private static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType(
                type -> Component.literal("Invalid direction: " + type)
        );

        public Direction parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readUnquotedString();
            Direction direction = EnumUtils.getEnum(Direction.class, string.toUpperCase());
            if (direction == null) {
                throw INVALID_TYPE_EXCEPTION.createWithContext(stringReader, string);
            } else {
                return direction;
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider
                    ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(s -> s.name().toLowerCase()), builder)
                    : Suggestions.empty();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static DirectionArgumentType directionArgumentType() {
            return new DirectionArgumentType();
        }

        public static Direction getDirection(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, Direction.class);
        }
    }
}
