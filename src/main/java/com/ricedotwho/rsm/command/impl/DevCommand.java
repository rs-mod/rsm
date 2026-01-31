package com.ricedotwho.rsm.command.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

@CommandInfo(aliases = "dev", description = "Developer command")
public class DevCommand extends Command {

    @Override
    public void execute(String[] args, String message) {
        if (args.length == 0) {
            ChatUtils.chat("Usage: loadfunny | loc | pos | days | onlinesecret | loadplayers | toggleboss | icltspmo | party | sbid | tablist | whereami | sb | csb | dungeonplayers | forcedev | setloc <loc>");
            return;
        }

        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase()) {
                    case "loc":
                        ChatUtils.chat("Location: %s", Loc.getArea());
                        break;
                    case "age":
                    case "day":
                    case "toggleboss":
                        Dungeon.inBoss = !Dungeon.inBoss;
                        ChatUtils.chat("Toggled inBoss to: %s", Dungeon.inBoss);
                        break;
                    case "icltspmo":
                        ClickGUI module = RSM.getModule(ClickGUI.class);
                        if(module == null) return;
                        module.getTruePlayerModifier().setValue(!module.getTruePlayerModifier().getValue());;
                        ChatUtils.chat("All player modifiers " + (module.getTruePlayerModifier().getValue() ? ChatFormatting.GREEN + "enabled" : ChatFormatting.RED + "disabled"));
                        break;
                    case "sbid":
                        ItemStack heldItem = mc.player.getInventory().getSelectedItem();
                        ChatUtils.chat("sbID: " + ItemUtils.getID(heldItem));
                        break;
                    case "whereami":
                        ChatUtils.chat("Location: " + Loc.getArea().getName() + ". Dungeon Floor: " + Loc.getFloor().getName() + ". Kuudra Tier: " + Loc.getKuudraTier() + ".");
                        break;
                    case "room":
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
                        break;
                }
            case 2:
                switch (args[0].toLowerCase()) {
                    case "setloc":
                        Loc.setArea(args[1]);
                        ChatUtils.chat("Set area to: %s", Loc.getArea());
                        break;
                }
        }
    }
}
