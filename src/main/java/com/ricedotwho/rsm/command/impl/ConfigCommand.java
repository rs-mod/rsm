package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.AddonContainer;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ConfigUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.module.Module;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.awt.*;
import java.io.File;
import java.util.List;

@CommandInfo(name = "config", aliases = "c", description = "Manages client configurations")
public class ConfigCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("save")
                        .executes(ctx -> {
                            ChatUtils.chat("Saving all modules");
                            ConfigUtils.saveConfig();
                            return 1;
                        })
                        .then(argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    Module module = RSM.getInstance().getModuleManager().getModuleFromID(id);
                                    if (module == null) {
                                        ChatUtils.chat("No module with the id %s was found!", id);
                                        return 1;
                                    }
                                    ConfigUtils.saveConfig(module);
                                    ChatUtils.chat("Saved config for %s", module.getName());
                                    return 1;
                                })
                        )
                )
                .then(literal("load")
                        .then(argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    Module module = RSM.getInstance().getModuleManager().getModuleFromID(id);
                                    if (module == null) {
                                        ChatUtils.chat("No module with the id %s was found!", id);
                                        return 1;
                                    }
                                    ConfigUtils.loadConfig(module);
                                    ChatUtils.chat("Loaded config for %s", module.getName());
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                            listConfigs(FileUtils.getCategoryFolder("config"));
                            return 1;
                        }))
                .then(literal("folder")
                        .executes(ctx -> {
                            openConfigFolder(FileUtils.getCategoryFolder("config"));
                            return 1;
                        }));
    }

    private void listConfigs(File folder) {
        if (folder == null || !folder.exists()) {
            ChatUtils.chat("Config folder does not exist.");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null && files.length > 0) {
            ChatUtils.chat("§7----- §fConfigs §7-----");
            for (File file : files) {
                String name = file.getName().replace(".json", "");
                ChatUtils.chat("§8> §f" + name);
            }
        } else {
            ChatUtils.chat("No saved configs found.");
        }
    }

    private void openConfigFolder(File folder) {
        try {
            if (folder != null && folder.exists()) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(folder);
                } else {
                    ChatUtils.chat("Unable to open config folder.");
                }
            } else {
                ChatUtils.chat("Config folder does not exist.");
            }
        } catch (Exception e) {
            ChatUtils.chat("Failed to open config folder.");
        }
    }
}
