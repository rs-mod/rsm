package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

@Getter
@ModuleInfo(aliases = "Click GUI", id = "ClickGUI", category = Category.RENDER, defaultKey = GLFW.GLFW_KEY_RIGHT_ALT, alwaysDisabled = true, hasKeybind = true)
public class ClickGUI extends Module {
    @Getter
    private final static ClickGUI instance = new ClickGUI();

    @Override
    public void onLoaded() {

    }
}