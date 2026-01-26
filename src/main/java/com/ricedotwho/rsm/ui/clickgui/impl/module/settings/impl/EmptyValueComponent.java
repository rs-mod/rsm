package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;

public class EmptyValueComponent extends ValueComponent<Setting<?>> {
    public EmptyValueComponent(Setting<?> setting, ModuleComponent moduleComponent) {
        super(setting, moduleComponent);
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        // intentionally empty
    }

    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {
        // intentionally empty
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {
        // intentionally empty
    }
}
