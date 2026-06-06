package com.ricedotwho.rsm.module.impl.render;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Getter
@ModuleInfo(aliases = "Tint", id = "ScreenTint", category = Category.RENDER)
public class ScreenTint extends Module {
    private static final ColourSetting colour = new ColourSetting("Colour", Colour.BLACK.alpha(0.25f));
    private static ScreenTint INSTANCE;

    public ScreenTint() {
        INSTANCE = this;
        this.registerProperty(
                colour
        );
    }

    public static boolean getEnabled() {
        return INSTANCE.isEnabled();
    }

    public static void drawTint(GuiGraphicsExtractor gfx) {
        Window window = mc.getWindow();
        gfx.fill(
                0,
                0,
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                colour.getValue().getRGB()
        );
    }
}
