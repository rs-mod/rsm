package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.module.impl.player.EquipmentHelper;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.Set;

@CommandInfo(name = "eqh", description = "Manages the Equipment Helper")
public class EquipmentHelperCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .executes(ctx -> {
                            if (mc.player == null) return 1;
                            String id = ItemUtils.getID(mc.player.getMainHandItem());
                            Set<String> autoClose = RSM.getModule(EquipmentHelper.class).getAutoCloseSet().getValue();
                            if (autoClose.add(id)) {
                                ChatUtils.chat("Added %s to autoclose", id);
                            } else {
                                ChatUtils.chat("%s is already on autoclose!", id);
                            }
                            return 1;
                        })
                        .then(argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id").toUpperCase();
                                    Set<String> autoClose = RSM.getModule(EquipmentHelper.class).getAutoCloseSet().getValue();
                                    if (autoClose.add(id)) {
                                        ChatUtils.chat("Added %s to autoclose", id);
                                    } else {
                                        ChatUtils.chat("%s is already on autoclose!", id);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .executes(ctx -> {
                            if (mc.player == null) return 1;
                            String id = ItemUtils.getID(mc.player.getMainHandItem());
                            Set<String> autoClose = RSM.getModule(EquipmentHelper.class).getAutoCloseSet().getValue();
                            autoClose.remove(id);
                            ChatUtils.chat("Removed %s from autoclose", id);
                            return 1;
                        })
                        .then(argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id").toUpperCase();
                                    Set<String> autoClose = RSM.getModule(EquipmentHelper.class).getAutoCloseSet().getValue();
                                    autoClose.remove(id);
                                    ChatUtils.chat("Removed %s from autoclose", id);
                                    return 1;
                                })
                        )
                );
    }
}
