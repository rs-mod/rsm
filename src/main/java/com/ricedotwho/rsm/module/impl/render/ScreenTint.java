package com.ricedotwho.rsm.module.impl.render;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Getter
@ModuleInfo(aliases = "Tint", id = "ScreenTint", category = Category.RENDER)
public class ScreenTint extends Module {
    private static final ScreenTint instance = new ScreenTint();
    private final ColorSetting color = new ColorSetting("Color", Color.BLACK.getARGBWithAlpha(0.25f));


    public static boolean getEnabled() {
        return instance.isEnabled();
    }

    public static void drawTint(GuiGraphicsExtractor gfx) {
        Window window = mc.getWindow();
        gfx.fill(
                0,
                0,
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                instance.color.getValue().getARGB()
        );
    }
}
