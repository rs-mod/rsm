package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.managers.location.Island;
import com.ricedotwho.rsm.managers.location.Location;
import com.ricedotwho.rsm.managers.map.handler.Dungeon;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

@Getter
@ModuleInfo(aliases = "Abilities", id = "Abilities", category = Category.DUNGEONS)
public class Abilities extends Module {
    private final KeybindSetting abilityBind = new KeybindSetting("Ability", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> {
        if (Location.getArea().is(Island.Dungeon) && Dungeon.isStarted()) {
            drop(true);
        }
        return false;
    }));

    private final KeybindSetting ultKeybind = new KeybindSetting("Ult", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> {
        if (Location.getArea().is(Island.Dungeon) && Dungeon.isStarted()) {
            drop(false);
        }
        return false;
    }));

    public Abilities() {
        this.registerProperty(
                ultKeybind,
                abilityBind
        );
    }

    protected void drop(boolean dropAll) {
        Scheduler.schedule(ClientTickEvent.Start.class, () -> {
            if (mc.player != null) mc.player.drop(dropAll);
        });
    }
}
