package com.ricedotwho.rsm.location;

import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import lombok.Getter;
import oshi.jna.platform.mac.SystemB;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum Island {
    Singleplayer("Singleplayer", "singleplayer"),
    PrivateIsland("Private Island", "dynamic"),
    Garden("The Garden", "garden"),
    SpiderDen("Spider's Den", "combat_1"),
    CrimsonIsle("Crimson Isle", "crimson_isle"),
    End("The End", "combat_3"),
    GoldMine("Gold Mine", "mining_1"),
    DeepCaverns("Deep Caverns", "mining_2"),
    DwarvenMines("Dwarven Mines", "mining_3"),
    CrystalHollows("Crystal Hollows", "crystal_hollows"),
    FarmingIsland("The Farming Islands", "farming_1"),
    Park("The Park", "foraging_1"),
    Dungeon("Catacombs", "dungeon"),
    DungeonHub("Dungeon Hub", "dungeon_hub"),
    Hub("Hub", "hub"),
    DarkAuction("Dark Auction", "dark_auction"),
    JerryWorkshop("Jerry's Workshop", "winter"),
    Kuudra("Kuudra", "kuudra"),
    Mineshaft("Mineshaft", "mineshaft"),
    Galatea("Galatea", "foraging_2"),
    Rift("The Rift", "rift"),
    // TODO: check Lotus Atoll & get id for Torrhus Canyon
    LotusAtoll("Lotus Atoll", "fishing_3"),
    Unknown("Unknown", null);

    private static final Map<String, Island> IDS = new HashMap<>();

    static {
        for (Island island : values()) {
            IDS.put(island.id, island);
        }
    }

    private final String name;
    private final String id;

    Island(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public boolean is(Island island) {
        if (island == Dungeon && Location.isForceSkyblock()) return true;
        return this.equals(island);
    }

    public static Island findByName(String name) {
        return Arrays.stream(Island.values())
                .filter(type -> name.contains(type.getName()))
                .findFirst()
                .orElse(Island.Unknown);
    }

    public static Island getByID(String id) {
        return IDS.get(id);
    }
}