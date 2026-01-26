package com.ricedotwho.rsm.command.api;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;

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
            ChatUtils.chat(EnumChatFormatting.RED + "Something went wrong running that command!");
        }

        if (!commandFound.get()) {
            ChatUtils.chat("Invalid command, '" + prefix + "help' for more info.");
        }

        event.setCanceled(true);
    }
}
