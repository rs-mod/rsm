package com.ricedotwho.rsm.component.impl.location;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Island {
    Singleplayer("Singleplayer"),
    PrivateIsland("Private Island"),
    Garden("The Garden"),
    SpiderDen("Spider's Den"),
    CrimsonIsle("Crimson Isle"),
    End("The End"),
    GoldMine("Gold Mine"),
    DeepCaverns("Deep Caverns"),
    DwarvenMines("Dwarven Mines"),
    CrystalHollows("Crystal Hollows"),
    FarmingIsland("The Farming Islands"),
    Park("The Park"),
    Dungeon("Catacombs"),
    DungeonHub("Dungeon Hub"),
    Hub("Hub"),
    DarkAuction("Dark Auction"),
    JerryWorkshop("Jerry's Workshop"),
    Kuudra("Kuudra"),
    Mineshaft("Mineshaft"),
    Galatea("Galatea"),
    Rift("The Rift"),
    Unknown("Unknown");

    private final String name;

    Island(String name) {
        this.name = name;
    }

    public boolean is(Island island) {
        if (island == Dungeon && Location.isForceSkyblock()) return true;
        return this.equals(island);
    }

    public String getEnumName() {
        return this.toString();
    }

    // Doesn't use the nice names, uses enum names instead
    public static Island findByEnumName(String name) {
        return Arrays.stream(Island.values())
                .filter(type -> name.equalsIgnoreCase(type.getEnumName()))
                .findFirst()
                .orElse(Island.Unknown);
    }

    public static Island findByName(String name) {
        return Arrays.stream(Island.values())
                .filter(type -> name.contains(type.getName()))
                .findFirst()
                .orElse(Island.Unknown);
    }
}