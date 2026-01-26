package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ConfigUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.module.Module;

import java.awt.*;
import java.io.File;

@CommandInfo(aliases = {"config", "c"}, description = "Manages client configurations")
public class ConfigCommand extends Command {
    private static final String CONFIG_USAGE = "Usage: .config <save | load | list | delete | folder> <name>";

    @Override
    public void execute(String[] args, String message) {
        if (args.length < 1) {
            ChatUtils.chat(CONFIG_USAGE);
            return;
        }

        String action = args[0].toLowerCase();
        File configFolder = FileUtils.getCategoryFolder("config");

        Module module;
        switch (action) {
            case "save":
            case "s":
                if (args.length < 2) {
                    ConfigUtils.saveConfig();
                    ChatUtils.chat("Saved all config");
                    return;
                }
                module = RSM.getInstance().getModuleManager().getModuleFromID(args[1]);

                if (module == null) {
                    ChatUtils.chat("No module with the name %s was found!", args[1]);
                    return;
                }

                ConfigUtils.saveConfig(module);

                ChatUtils.chat("Saved config for %s", module.getName());
                break;

            case "load":
            case "l":
                if (args.length < 2) {
                    ChatUtils.chat("Please specify a module to load.");
                    return;
                }

                module = RSM.getInstance().getModuleManager().getModuleFromID(args[1]);

                if (module == null) {
                    ChatUtils.chat("No module with the name %s was found!", args[1]);
                    return;
                }

                ConfigUtils.loadConfig(module);

                ChatUtils.chat("Loaded config for %s", module.getName());
                break;

            case "list":
            case "ls":
                listConfigs(configFolder);
                break;

            case "delete":
            case "d":
                if (args.length < 2) {
                    ChatUtils.chat("Please specify a config name to delete.");
                    return;
                }
                deleteConfig(args[1]);
                break;

            case "folder":
            case "f":
                openConfigFolder(configFolder);
                break;

            default:
                ChatUtils.chat("Unknown action: " + action);
                ChatUtils.chat(CONFIG_USAGE);
                break;
        }
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

    private void deleteConfig(String name) {
        File configFile = FileUtils.getSaveFileInCategory("config", name + ".json");
        if (configFile.exists()) {
            try {
                org.apache.commons.io.FileUtils.forceDelete(configFile);
                ChatUtils.chat("Deleted config: " + name);
            } catch (Exception e) {
                ChatUtils.chat("Failed to delete config: " + name);
            }
        } else {
            ChatUtils.chat("Config does not exist: " + name);
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
