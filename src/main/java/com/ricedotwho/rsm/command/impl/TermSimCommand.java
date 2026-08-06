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
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.ui.termsim.TermSimScreen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.EnumUtils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandInfo(name = "termsim", aliases = "ts", description = "Opens termsim")
public class TermSimCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(argument("type", new TerminalArgumentType())
                        .executes(ctx -> {
                            TerminalType type = TerminalArgumentType.get(ctx, "type");
                            Scheduler.tick(() -> TermSimScreen.open(type));
                            return 1;
                        })
                );
    }

    private static class TerminalArgumentType implements ArgumentType<TerminalType> {
        private static final Collection<String> EXAMPLES = Set.of("order");
        private static final Set<String> VALUES = Set.of("order");//TerminalType.getValues().stream().map(s -> s.name().toLowerCase()).collect(Collectors.toSet());
        private static final DynamicCommandExceptionType INVALID_TERMINAL_EXCEPTION = new DynamicCommandExceptionType(
                room -> Component.literal("Invalid terminal type : " + room)
        );

        public TerminalType parse(StringReader stringReader) throws CommandSyntaxException {
            final String string = stringReader.getRemaining();
            stringReader.setCursor(stringReader.getTotalLength());
            if (!VALUES.contains(string)) {
                throw INVALID_TERMINAL_EXCEPTION.createWithContext(stringReader, string);
            } else {
                TerminalType type = EnumUtils.getEnum(TerminalType.class, string.toUpperCase() ,TerminalType.NONE);
                if (type == TerminalType.NONE)  {
                    throw INVALID_TERMINAL_EXCEPTION.createWithContext(stringReader, string);
                }
                return type;
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

        public static TerminalType get(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, TerminalType.class);
        }
    }
}
