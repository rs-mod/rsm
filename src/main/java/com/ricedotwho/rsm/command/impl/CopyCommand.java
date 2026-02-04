package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ConfigUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

@CommandInfo(name = "copy", description = "Copies text to clipboard")
public class CopyCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(argument("contents", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String contents = StringArgumentType.getString(ctx, "contents");
                            Toolkit.getDefaultToolkit().getSystemClipboard()
                                    .setContents(new StringSelection(contents), null);
                            return 1;
                        })
                );
    }
}
