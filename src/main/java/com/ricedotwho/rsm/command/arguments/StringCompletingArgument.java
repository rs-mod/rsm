package com.ricedotwho.rsm.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class StringCompletingArgument implements ArgumentType<String> {
    private static final DynamicCommandExceptionType INVALID_ARGUMENT_EXCEPTION = new DynamicCommandExceptionType(
            argument -> Component.literal("Invalid argument: " + argument)
    );
    private final boolean greedy;
    private final Collection<String> examples;
    private final Supplier<Collection<String>> suggestions;

    public StringCompletingArgument(Supplier<Collection<String>> suggestions, Collection<String> examples = new ArrayList<String>(), boolean greedy = false) {
        this.suggestions = suggestions;
        this.examples = examples;
        this.greedy = greedy;
    }

    public String parse(StringReader stringReader) throws CommandSyntaxException {
        String string;
        if (greedy) {
            string = stringReader.getRemaining();
            stringReader.setCursor(stringReader.getTotalLength());
        } else {
            string = stringReader.readUnquotedString();
        }
        if (!suggestions.get().contains(string)) {
            throw INVALID_ARGUMENT_EXCEPTION.createWithContext(stringReader, string);
        } else {
            return string;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider
                ? SharedSuggestionProvider.suggest(suggestions.get(), builder)
                : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return examples;
    }

    public static String get(CommandContext<ClientSuggestionProvider> context, String name) {
        return context.getArgument(name, String.class);
    }
}
