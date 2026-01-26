package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.AddonContainer;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;

@CommandInfo(aliases = {"addon"}, description = "Manages addons")
public class AddonCommand extends Command {
    private static final String USAGE = "Usage: .addon <reload | load | unload | list> <?id>";

    @Override
    public void execute(String[] args, String message) {
        if (args.length < 1) {
            ChatUtils.chat(USAGE);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "reload":
            case "r":
                if (args.length < 2) {
                    ChatUtils.chat("Reloading all addons");
                    RSM.getInstance().getAddonLoader().reload();
                } else {
                    ChatUtils.chat("Reloading %s", args[1]);
                    RSM.getInstance().getAddonLoader().reload(args[1]);
                }
                break;

            case "load":
            case "l":
                if (args.length < 2) {
                    ChatUtils.chat("Loading all addons");
                    RSM.getInstance().getAddonLoader().load();
                } else {
                    ChatUtils.chat("Loading %s", args[1]);
                    RSM.getInstance().getAddonLoader().load(args[1]);
                }
                break;
            case "unload":
            case "u":
                if (args.length < 2) {
                    ChatUtils.chat("Unloading all addons");
                    RSM.getInstance().getAddonLoader().unload();
                } else {
                    ChatUtils.chat("Unloading %s", args[1]);
                    RSM.getInstance().getAddonLoader().unload(args[1]);
                }
                break;
            case "list":
                StringBuilder sb = new StringBuilder();
                for (AddonContainer addon : RSM.getInstance().getAddonLoader().getAddons()) {
                    sb.append("\n").append(addon.getAddon().getName()).append(" (").append(addon.getMeta().getId()).append(")");
                }

                ChatUtils.chat("Addons: " + sb);
                break;

            default:
                ChatUtils.chat("Unknown action: " + action);
                ChatUtils.chat(USAGE);
                break;
        }
    }
}
