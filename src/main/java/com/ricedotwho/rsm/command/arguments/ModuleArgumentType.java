package com.ricedotwho.rsm.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsm.RSM;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import com.ricedotwho.rsm.module.Module;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleArgumentType implements ArgumentType<Module> {
        private static final Collection<String> EXAMPLES = Stream.of("ClickGUI").collect(Collectors.toList());
        private static final DynamicCommandExceptionType INVALID_MODULE_EXCEPTION = new DynamicCommandExceptionType(
                module -> Component.literal("Invalid module: " + module)
        );

        public Module parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readUnquotedString();
            Module module = RSM.getInstance().getModuleManager().getModuleFromID(string);
            if (module == null) {
                throw INVALID_MODULE_EXCEPTION.createWithContext(stringReader, string);
            } else {
                return module;
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider
                    ? SharedSuggestionProvider.suggest(getValues(), builder)
                    : Suggestions.empty();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static ModuleArgumentType moduleArgument() {
            return new ModuleArgumentType();
        }

        public static Module getModule(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, Module.class);
        }

        private List<String> getValues() {
            return RSM.getInstance().getConfigGui() == null ? List.of() : RSM.getInstance().getConfigGui().moduleList.stream().filter(m -> !m.getModule().getInfo().alwaysDisabled()).map(m -> m.getModule().getID()).toList();
        }
    }