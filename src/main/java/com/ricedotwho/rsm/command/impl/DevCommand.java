package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.AddonContainer;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@CommandInfo(name = "dev", description = "Developer command")
public class DevCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("loc")
                        .executes(ctx -> {
                            ChatUtils.chat("Location: %s", Location.getArea());
                            return 1;
                        }))
                .then(literal("dungeonplayers")
                        .executes(ctx -> {
                            ChatUtils.chat("Dungeon players: %s", Dungeon.getPlayers().stream().toList());
                            return 1;
                        }))
                .then(literal("toggleboss")
                        .executes(ctx -> {
                            Dungeon.setInBoss(!Dungeon.isInBoss());
                            ChatUtils.chat("Toggled inBoss to: %s", Dungeon.isInBoss());
                            return 1;
                        }))
                .then(literal("icltspmo")
                        .executes(ctx -> {
                            ClickGUI module = RSM.getModule(ClickGUI.class);
                            assert module != null;
                            module.getTruePlayerModifier().setValue(!module.getTruePlayerModifier().getValue());
                            ChatUtils.chat("All player modifiers " + (module.getTruePlayerModifier().getValue() ? ChatFormatting.GREEN + "enabled" : ChatFormatting.RED + "disabled"));
                            return 1;
                        }))
                .then(literal("sbid")
                        .executes(ctx -> {
                            ItemStack heldItem = mc.player.getInventory().getSelectedItem();
                            ChatUtils.chat("sbID: " + ItemUtils.getID(heldItem));
                            return 1;
                        }))
                .then(literal("whereami")
                        .executes(ctx -> {
                            ChatUtils.chat("Location: " + Location.getArea().getName() + ". Dungeon Floor: " + Location.getFloor().getName() + ". Kuudra Tier: " + Location.getKuudraTier() + ".");
                            return 1;
                        }))
                .then(literal("room")
                        .executes(ctx -> {
                            Room room = Map.getCurrentRoom();
                            if (room == null) {
                                ChatUtils.chat(ChatFormatting.RED + "Room is null");
                            } else {
                                UniqueRoom uni = room.getUniqueRoom();
                                if (uni == null) {
                                    ChatUtils.chat("Unique is null! %s", room.getData().name());
                                } else {
                                    if (uni.getMainRoom() == null) {
                                        ChatUtils.chat("Room: %s, unique: %s, rotation: %s, main is null!", room.getData().name(), uni.getName(), uni.getRotation());
                                    } else {
                                        ChatUtils.chat("Room: %s, x: %s, z: %s, rotation: %s", room.getData().name(), uni.getMainRoom().getX(), uni.getMainRoom().getZ(), uni.getRotation());
                                    }
                                }
                            }
                            return 1;
                        }))
                .then(literal("setlocation")
                        .then(argument("location", StringArgumentType.word())
                                .executes(ctx -> {
                                    Location.setArea(StringArgumentType.getString(ctx, "location"));
                                    ChatUtils.chat("Set area to: %s", Location.getArea());
                                    return 1;
                                })
                        )
                );
    }
}
