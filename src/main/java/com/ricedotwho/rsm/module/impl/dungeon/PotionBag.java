package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import lombok.Getter;

import java.util.List;

@Getter
@ModuleInfo(aliases = "Potion Bag", id = "PotionBag", category = Category.DUNGEONS)
public class PotionBag extends Module {
    private final MultiBoolSetting floors = new MultiBoolSetting("Floors", List.of("M4", "M5", "M6", "M7"), List.of("M7"));

    public PotionBag() {
        this.registerProperty(
                floors
        );
    }

    @SubscribeEvent
    public void onDungeonJoined(DungeonEvent.Joined event) {
        if (floors.get(event.getFloor().getName()) && mc.getConnection() != null) {
            mc.getConnection().sendCommand("potionbag");
        }
    }
}
