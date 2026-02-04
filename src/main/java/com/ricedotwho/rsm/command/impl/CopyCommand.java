package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

@CommandInfo(aliases = "copy", description = "Copies text to clipboard")
public class CopyCommand extends Command {

    @Override
    public void execute(String[] args, String message) {
        if (args.length == 0) {
            ChatUtils.chat("Usage: .copy some text to copy");
            return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(" ").append(args[i]);
        }
        clipboard.setContents(new StringSelection(sb.toString().trim()), null);
    }

    @Override
    public List<String> complete(String[] args, String current) {
        return List.of();
    }
}
