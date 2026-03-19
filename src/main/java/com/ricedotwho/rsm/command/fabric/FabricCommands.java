package com.ricedotwho.rsm.command.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class FabricCommands {
    private final Map<String, String> shortenings = new HashMap<>();
    static {
        shortenings.put("f0", "joindungeon catacombs_entrance");
        shortenings.put("f1", "joindungeon catacombs 1");
        shortenings.put("f2", "joindungeon catacombs 2");
        shortenings.put("f3", "joindungeon catacombs 3");
        shortenings.put("f4", "joindungeon catacombs 4");
        shortenings.put("f5", "joindungeon catacombs 5");
        shortenings.put("f6", "joindungeon catacombs 6");
        shortenings.put("f7", "joindungeon catacombs 7");

        shortenings.put("m1", "joindungeon master_catacombs 1");
        shortenings.put("m2", "joindungeon master_catacombs 2");
        shortenings.put("m3", "joindungeon master_catacombs 3");
        shortenings.put("m4", "joindungeon master_catacombs 4");
        shortenings.put("m5", "joindungeon master_catacombs 5");
        shortenings.put("m6", "joindungeon master_catacombs 6");
        shortenings.put("m7", "joindungeon master_catacombs 7");

        shortenings.put("t1", "joindungeon kuudra_normal");
        shortenings.put("t2", "joindungeon kuudra_hot");
        shortenings.put("t3", "joindungeon kuudra_burning");
        shortenings.put("t4", "joindungeon kuudra_fiery");
        shortenings.put("t5", "joindungeon kuudra_infernal");

        shortenings.put("dh", "warp dungeon_hub");
        shortenings.put("dn", "warp dungeon_hub");
        shortenings.put("d", "warp dungeon_hub");
    }

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : shortenings.entrySet()) {
            registerShortening(dispatcher, entry);
        }
    }

    private void registerShortening(CommandDispatcher<FabricClientCommandSource> dispatcher, Map.Entry<String, String> entry) {
        dispatcher.register(ClientCommandManager.literal(entry.getKey()).executes(ctx -> {
            if (Minecraft.getInstance().getConnection() == null) return 0;
            Minecraft.getInstance().getConnection().sendCommand(entry.getValue());
            return 1;
        }));
    }
}