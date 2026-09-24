package com.ricedotwho.rsm.command.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.ricedotwho.rsm.core.Init;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class FabricCommands {

    @Init
    private void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> FabricCommands.register(dispatcher));
    }

    private final Map<String, String> shortenings = Map.ofEntries(
            Map.entry("e", "joindungeon catacombs_entrance"),
            Map.entry("f1", "joindungeon catacombs 1"),
            Map.entry("f2", "joindungeon catacombs 2"),
            Map.entry("f3", "joindungeon catacombs 3"),
            Map.entry("f4", "joindungeon catacombs 4"),
            Map.entry("f5", "joindungeon catacombs 5"),
            Map.entry("f6", "joindungeon catacombs 6"),
            Map.entry("f7", "joindungeon catacombs 7"),

            Map.entry("m1", "joindungeon master_catacombs 1"),
            Map.entry("m2", "joindungeon master_catacombs 2"),
            Map.entry("m3", "joindungeon master_catacombs 3"),
            Map.entry("m4", "joindungeon master_catacombs 4"),
            Map.entry("m5", "joindungeon master_catacombs 5"),
            Map.entry("m6", "joindungeon master_catacombs 6"),
            Map.entry("m7", "joindungeon master_catacombs 7"),

            Map.entry("t1", "joindungeon kuudra_normal"),
            Map.entry("t2", "joindungeon kuudra_hot"),
            Map.entry("t3", "joindungeon kuudra_burning"),
            Map.entry("t4", "joindungeon kuudra_fiery"),
            Map.entry("t5", "joindungeon kuudra_infernal"),

            // may get overridden by other mods

            Map.entry("dh", "warp dungeon_hub"),
            Map.entry("dn", "warp dungeon_hub"),
            Map.entry("d", "warp dungeon_hub"),

            Map.entry("pw", "p warp"),
            Map.entry("pd", "p disband"),
            Map.entry("pko", "p kickoffline")
    );

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : shortenings.entrySet()) {
            registerShortening(dispatcher, entry);
        }

        // special command
        dispatcher.register(ClientCommands.literal("pt")
                .then(ClientCommands.argument("name", StringArgumentType.string())
                        .executes(ctx -> {
                            if (Minecraft.getInstance().getConnection() == null) return 0;
                            Minecraft.getInstance().getConnection().sendCommand("p transfer " + StringArgumentType.getString(ctx, "name"));
                            return 1;
                        })
                )
        );
    }

    private void registerShortening(CommandDispatcher<FabricClientCommandSource> dispatcher, Map.Entry<String, String> entry) {
        dispatcher.register(ClientCommands.literal(entry.getKey()).executes(ctx -> {
            if (Minecraft.getInstance().getConnection() == null) return 0;
            Minecraft.getInstance().getConnection().sendCommand(entry.getValue());
            return 1;
        }));
    }
}