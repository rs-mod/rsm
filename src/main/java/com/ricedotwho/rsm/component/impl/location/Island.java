package com.ricedotwho.rsm.component.impl.location;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.other.ConfigQOL;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Island {
    Singleplayer("Singleplayer"),
    PrivateIsland("Private Island"),
    Garden("The Garden"),
    SpiderDen("Spider's Den"),
    CrimsonIsle("Crimson Isle"),
    TheEnd("The End"),
    GoldMine("Gold Mine"),
    DeepCaverns("Deep Caverns"),
    DwarvenMines("Dwarven Mines"),
    CrystalHollows("Crystal Hollows"),
    FarmingIsland("The Farming Islands"),
    ThePark("The Park"),
    Dungeon("Catacombs"),
    DungeonHub("Dungeon Hub"),
    Hub("Hub"),
    DarkAuction("Dark Auction"),
    JerryWorkshop("Jerry's Workshop"),
    Kuudra("Kuudra"),
    Mineshaft("Mineshaft"),
    Unknown("(Unknown)");

    private final String name;

    Island(String name) {
        this.name = name;
    }

    public boolean is(Island island) {
        if (island == Dungeon && RSM.getModule(ConfigQOL.class).isForceSkyblock()) return true;
        return this.equals(island);
    }

    public static Island findByName(String name) {
        return Arrays.stream(Island.values())
                .filter(type -> name.contains(type.getName()))
                .findFirst()
                .orElse(Island.Unknown);
    }
}