package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.AddonContainer;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "addon", description = "Manages addons")
public class AddonCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("reload")
                        .executes(ctx -> {
                            ChatUtils.chat("Reloading all addons");
                            RSM.getInstance().getAddonLoader().reload();
                            return 1;
                        })
                        .then(argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    ChatUtils.chat("Reloading %s", id);
                                    RSM.getInstance().getAddonLoader().reload(id);
                                    return 1;
                                })
                        )
                )
                .then(literal("load")
                        .executes(ctx -> {
                            ChatUtils.chat("Loading all addons");
                            RSM.getInstance().getAddonLoader().load();
                            return 1;
                        })
                        .then(argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    ChatUtils.chat("Loading %s", id);
                                    RSM.getInstance().getAddonLoader().load(id);
                                    return 1;
                                })
                        )
                )
                .then(literal("unload")
                        .executes(ctx -> {
                            ChatUtils.chat("Unloading all addons");
                            RSM.getInstance().getAddonLoader().unload();
                            return 1;
                        })
                        .then(argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    ChatUtils.chat("Unloading %s", id);
                                    RSM.getInstance().getAddonLoader().unload(id);
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder();
                            for (AddonContainer addon : RSM.getInstance().getAddonLoader().getAddons()) {
                                sb.append("\n").append(addon.getMeta().getName()).append(" (").append(addon.getMeta().getId()).append(")");
                            }

                            ChatUtils.chat("Addons: " + sb);
                            return 1;
                        }));
    }
}
