package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Keybind;
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
    }));

    private final KeybindSetting ultKeybind = new KeybindSetting("Ult", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> {
        if (Location.getArea().is(Island.Dungeon) && Dungeon.isStarted()) {
            drop(false);
        }
    }));

    public Abilities() {
        this.registerProperty(
                ultKeybind,
                abilityBind
        );
    }

    protected void drop(boolean dropAll) {
        TaskComponent.onTick(() -> {
            if (mc.player != null) mc.player.drop(dropAll);
        });
    }
}
