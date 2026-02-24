package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.AddonContainer;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.SbStatTracker;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.ChunkAccess;

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
                .then(literal("itemdata")
                        .executes(ctx -> {
                            ItemStack heldItem = mc.player.getInventory().getSelectedItem();
                            ChatUtils.chat("Data: " + ItemUtils.getCustomData(heldItem));
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
                                        ChatUtils.chat("Room: %s, unique: %s, rotation: %s, main is null! (tiles: %s)", room.getData().name(), uni.getName(), uni.getRotation(), uni.getTiles());
                                    } else {
                                        ChatUtils.chat("Room: %s, x: %s, z: %s, rotation: %s (tiles: %s)", room.getData().name(), uni.getMainRoom().getX(), uni.getMainRoom().getZ(), uni.getRotation(), uni.getTiles());
                                    }
                                }
                            }
                            return 1;
                        }))
                .then(literal("roompos")
                        .executes(ctx -> {
                            if (mc.player == null || Map.getCurrentRoom() == null || Map.getCurrentRoom().getUniqueRoom().getMainRoom() == null) return 1;

                            ChatUtils.chat("Relative position: %s",
                                    Map.getCurrentRoom().getUniqueRoom().getMainRoom().getRelativePosition(new Pos(mc.player.position())));

                            return 1;
                        })
                )
                .then(literal("getcore")
                        .executes(ctx -> {
                            Room room = Map.getCurrentRoom();
                            ChunkAccess chunk = mc.level.getChunk(new BlockPos(room.getX(), 0, room.getZ()));
                            int roomCore = ScanUtils.getCore(room.getX(), room.getZ(), room.getRoofHeight(), chunk);
                            ChatUtils.chat("Core: %s", roomCore);
                            return 1;
                        })
                )
                .then(literal("setlocation")
                        .then(argument("location", StringArgumentType.word())
                                .executes(ctx -> {
                                    Location.setArea(StringArgumentType.getString(ctx, "location"));
                                    ChatUtils.chat("Set area to: %s", Location.getArea());
                                    return 1;
                                })
                        )
                )
                .then(literal("lore")
                        .executes(ctx -> {
                            ChatUtils.chat("Lore: %s", ItemUtils.getLore(mc.player.getInventory().getSelectedItem()));
                            return 1;
                        })
                )
                .then(literal("cleanlore")
                        .executes(ctx -> {
                            ChatUtils.chat("Clean Lore: %s", ItemUtils.getCleanLore(mc.player.getInventory().getSelectedItem()));
                            return 1;
                        })
                )
                .then(literal("stats")
                        .executes(ctx -> {
                            ChatUtils.chat("Stats: %s", SbStatTracker.getStats());
                            return 1;
                        })
                )
                .then(literal("campos")
                        .executes(ctx -> {
                            ChatUtils.chat("CameraPos: %s", CameraHandler.getCameraPos());
                            return 1;
                        })
                );
    }
}
