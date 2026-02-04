package com.ricedotwho.rsm.command.api;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.data.Manager;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerChatEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.regex.Pattern;

public class CommandManager extends Manager<Command> implements Accessor {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    @Getter
    private final CommandDispatcher<ClientSuggestionProvider> dispatcher = new CommandDispatcher<>();

    @Override
    public void put(Command command) {
        LiteralArgumentBuilder<ClientSuggestionProvider> root = command.build();

        if (!root.getLiteral().equals(command.name())) {
            throw new IllegalStateException("Command literal must match command.name(): " + command.name());
        }

        this.getMap().put(command.getClass(), command);
        dispatcher.register(root);

        // aliases
        for (String s : command.getAliases()){
            dispatcher.register(Command.literal(s)
                    .redirect(dispatcher.getRoot().getChild(command.name()))
            );
        }
    }

    @Override
    public void remove(Command command) {
        dispatcher.getRoot().getChildren().removeIf(node ->
                node.getName().equals(command.name()) ||
                        Arrays.asList(command.getAliases()).contains(node.getName())
        );
    }

    @SubscribeEvent
    public void onPlayerChat(PlayerChatEvent event) {
        String message = event.getMessage();

        String prefix = RSM.getModule(ClickGUI.class).getCommandPrefix().getValue();
        if (!message.startsWith(prefix)) return;

        message = message.substring(prefix.length());

        // this might fuck stuff up for other languages. i think it fucked up for chinese before
        message = Normalizer.normalize(message, Normalizer.Form.NFKC).trim();

        String[] args = WHITESPACE.split(message);
        if (args.length == 0) return;

        try {
            execute(message, mc.player.connection.getSuggestionsProvider());
        } catch (CommandSyntaxException e) {
            Component msg = ComponentUtils.fromMessage(e.getRawMessage());
            ChatUtils.chat(msg);
        }

        event.setCancelled(true);
    }

    @SuppressWarnings("UnusedReturnValue")
    public int execute(String input, ClientSuggestionProvider source) throws CommandSyntaxException {
        return dispatcher.execute(input, source);
    }
}
