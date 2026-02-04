package com.ricedotwho.rsm.command.api;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.data.Manager;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerChatEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.FormattedCharSequence;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class CommandManager extends Manager<Command> {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

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

        final AtomicBoolean commandFound = new AtomicBoolean(false);
        String finalMessage = message;

        try {
            List<Command> commands = new ArrayList<>(getMap().values());
            for (Command cmd : commands) {
                if (Arrays.stream(cmd.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                    commandFound.set(true);
                    String[] arr = Arrays.copyOfRange(args, 1, args.length);
                    cmd.execute(arr, finalMessage);
                }
            }
        } catch (Exception e) {
            RSM.getLogger().error("Something went wrong running {}", args[0], e);
            ChatUtils.chat(ChatFormatting.RED + "Something went wrong running that command!");
        }

        if (!commandFound.get()) {
            ChatUtils.chat("Invalid command, \"" + prefix + "help\" for more info.");
        }

        event.setCancelled(true);
    }

    /// i don't know how to code apr
    public Suggestions complete(String string, int cursor) {
        String raw = string.substring(1);

        int start = getStart(string, cursor);
        String current = raw.substring(Math.max(0, start - 1), cursor - 1);
        String[] args = raw.substring(0, cursor - 1).trim().split("\\s+");

        SuggestionsBuilder builder = new SuggestionsBuilder(string, start);
        List<Command> commands = new ArrayList<>(getMap().values());

        if (args.length == 0 || args.length == 1 && (args[0].isEmpty() || !current.isEmpty())) {
            String prefix = RSM.getModule(ClickGUI.class).getCommandPrefix().getValue();
            for (Command cmd : commands) {
                if (Arrays.stream(cmd.getAliases()).anyMatch(alias -> alias.startsWith(current))) {
                    for (String s : cmd.getAliases()) {
                        builder.suggest(prefix + s); // this is so cursed idk why it removed the prefix from chat :sob:
                    }
                }
            }
        } else {
            for (Command cmd : commands) {
                if (Arrays.stream(cmd.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                    for (String s : cmd.complete(args, current)) {
                        builder.suggest(s);
                    }
                }
            }
        }

        return builder.build();
    }

    private int getStart(String input, int cursor) {
        int i = cursor - 1;
        while (i >= 0 && input.charAt(i) != ' ') {
            i--;
        }
        return i + 1;
    }
}
