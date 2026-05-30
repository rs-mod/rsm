package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.Scheduler;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.impl.render.itemmodifier.ItemModifier;
import com.ricedotwho.rsm.ui.itemmodifier.ItemModifierGui;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.item.ItemStack;

@CommandInfo(name = "itemmodifier", aliases = {"item"}, description = "Visual item name overrides by UUID")
public class ItemModifierCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    Scheduler.schedule(ClientTickEvent.Start.class, ItemModifierGui::open);
                    return 1;
                })
.then(literal("open")
        .executes(ctx -> {
            Scheduler.schedule(ClientTickEvent.Start.class, ItemModifierGui::open);
            return 1;
        })
)
.then(literal("gui")
        .executes(ctx -> {
            Scheduler.schedule(ClientTickEvent.Start.class, ItemModifierGui::open);
            return 1;
        })
                )
                .then(literal("set")
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    if (mc.player == null) {
                                        return 0;
                                    }

                                    ItemStack stack = mc.player.getMainHandItem();
                                    if (stack.isEmpty()) {
                                        ChatUtils.chat("No held item");
                                        return 1;
                                    }

                                    String uuid = ItemUtils.getUUID(stack);
                                    if (uuid.isBlank()) {
                                        ChatUtils.chat("Held item has no UUID");
                                        return 1;
                                    }

                                    String name = StringArgumentType.getString(ctx, "name").trim();
                                    if (name.isBlank()) {
                                        ChatUtils.chat("Name cannot be blank");
                                        return 1;
                                    }

                                    ItemModifier.put(uuid, name, null);
                                    ChatUtils.chat("Set name for %s", uuid);
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .executes(ctx -> {
                            if (mc.player == null) {
                                return 0;
                            }

                            ItemStack stack = mc.player.getMainHandItem();
                            if (stack.isEmpty()) {
                                ChatUtils.chat("No held item");
                                return 1;
                            }

                            String uuid = ItemUtils.getUUID(stack);
                            if (uuid.isBlank()) {
                                ChatUtils.chat("Held item has no UUID");
                                return 1;
                            }

                            if (ItemModifier.remove(uuid)) {
                                ChatUtils.chat("Removed name for %s", uuid);
                            } else {
                                ChatUtils.chat("No name found for %s", uuid);
                            }
                            return 1;
                        })
                )
                .then(literal("list")
                        .executes(ctx -> {
                            if (ItemModifier.getData().isEmpty()) {
                                ChatUtils.chat("No item names set.");
                                return 1;
                            }

                            ItemModifier.getData().forEach((uuid, value) ->
                                    ChatUtils.chat("%s -> %s (%s)", uuid, value.name, value.enabled ? "on" : "off"));
                            return 1;
                        })
                )
                .then(literal("load")
                        .executes(ctx -> {
                            ItemModifier.load();
                            ChatUtils.chat("Loaded item modifiers");
                            return 1;
                        })
                );
    }
}

