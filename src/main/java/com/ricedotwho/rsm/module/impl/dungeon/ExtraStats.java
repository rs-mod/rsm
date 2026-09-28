package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;

@ModuleInfo(aliases = "Extra Stats", id = "extra-stats", category = Category.DUNGEONS)
public class ExtraStats extends Module {
    @SuppressWarnings("unused")
    private static final ExtraStats instance = new ExtraStats();

    @SubscribeEvent
    public void onDungeonEnd(DungeonEvent.End end) {
        if (mc.getConnection() == null) return;
        mc.getConnection().sendCommand("showextrastats");
    }
}
