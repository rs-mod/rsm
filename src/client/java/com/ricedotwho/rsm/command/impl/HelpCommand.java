package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;

@CommandInfo(aliases = { "help", "h" }, description = "Shows all available commands")
public class HelpCommand extends Command {
    @Override
    public void execute(String[] args, String message) {
        ChatUtils.chat("Commands:");
        for (Command c : RSM.getInstance().getCommandManager().getMap().values()) {
            ChatUtils.chatClean("§d" + RSM.getModule(ClickGUI.class).getCommandPrefix().getValue() + c.getInfo().aliases()[0] + " §3» §7" +
                    (c.getInfo().description().isEmpty() ? "" : c.getInfo().description()));
        }
    }
}
