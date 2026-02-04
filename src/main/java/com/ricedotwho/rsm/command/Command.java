package com.ricedotwho.rsm.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public abstract class Command implements Accessor {

    private final CommandInfo info;

    public Command() {
        if (this.getClass().isAnnotationPresent(CommandInfo.class)) {
            info = this.getClass().getAnnotation(CommandInfo.class);
        } else {
            throw new RuntimeException("Command doesn't have a CommandInfo annotation");
        }
    }

    public final String[] getAliases() {
        return this.info.aliases();
    }

    public final String name() {
        return this.info.name();
    }

    public final CommandInfo getInfo() {
        return getClass().getAnnotation(CommandInfo.class);
    }

    public abstract LiteralArgumentBuilder<ClientSuggestionProvider> build();

    /// tspmo
    public static LiteralArgumentBuilder<ClientSuggestionProvider> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<ClientSuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
