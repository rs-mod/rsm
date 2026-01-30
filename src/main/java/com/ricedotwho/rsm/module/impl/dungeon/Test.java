package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

@Getter // please don't use spaces in the id, also avoid duplicate ids, or loading/saving will break
@ModuleInfo(aliases = "Test Module", id = "test", category = Category.DUNGEONS)
public class Test extends Module {

    private final KeybindSetting keybindSetting = new KeybindSetting("key", new Keybind(GLFW.GLFW_KEY_UNKNOWN, true, false, () -> {
        ChatUtils.chat("wow a keybind");
    }));

    public Test() {
        this.registerProperty(
                // todo: register settings
                keybindSetting
        );
    }

    @Override
    public void onEnable() {
        keybindSetting.getValue().register();
    }

    @Override
    public void onDisable() {
        keybindSetting.getValue().unregister();
    }

    @Override
    public void reset() {

    }
}
