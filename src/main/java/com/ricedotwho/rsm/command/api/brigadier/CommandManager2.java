package com.ricedotwho.rsm.command.api.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerChatEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;

import java.text.Normalizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class CommandManager2 extends ModComponent {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    @Getter
    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    public CommandManager2() {
        super("CommandManager2");

        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("route")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String msg = StringArgumentType.getString(context, "name");
                                    ChatUtils.chat("h " + msg);
                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                                .executes(ctx -> {
                                    ChatUtils.chat("aaaaabbbbb");
                                    return 1;
                                })
                        )
        );
    }

    @SubscribeEvent
    public void onPlayerChat(PlayerChatEvent event) {
        String message = event.getMessage();

        String prefix = ".";//RSM.getModule(ClickGUI.class).getCommandPrefix().getValue();
        if (!message.startsWith(prefix)) return;

        message = message.substring(prefix.length());

        // this might fuck stuff up for other languages. i think it fucked up for chinese before
        message = Normalizer.normalize(message, Normalizer.Form.NFKC).trim();

        String[] args = WHITESPACE.split(message);
        if (args.length == 0) return;

        final AtomicBoolean commandFound = new AtomicBoolean(false);

        try {
            commandFound.set(execute(message, new CommandSource()) == 1);
        } catch (Exception e) {
            RSM.getLogger().error("Something went wrong running {}", args[0], e);
            ChatUtils.chat(ChatFormatting.RED + "Something went wrong running that command!");
        }

        if (!commandFound.get()) {
            ChatUtils.chat("Invalid command, \"" + prefix + "help\" for more info.");
        }

        event.setCancelled(true);
    }

    public CompletableFuture<Suggestions> complete(String input, int cursor, CommandSource source) {
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, source), cursor);
    }

    public int execute(String input, CommandSource source) throws CommandSyntaxException {
        return dispatcher.execute(input, source);
    }
}
