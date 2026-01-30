package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;

public class BooleanValueComponent extends ValueComponent<BooleanSetting> {
    public BooleanValueComponent(BooleanSetting setting, ModuleComponent moduleComponent) {
        super(setting, moduleComponent);
    }

    /**
     * Shit impl for enabled or not
     */
    public BooleanValueComponent(ModuleComponent moduleComponent) {
        super(null, moduleComponent);
    }

    private final StopWatch stopWatch = new StopWatch();
    private boolean lastHovered = false;

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        boolean isToggled = getSetting() != null ? getSetting().getValue() : getParent().getModule().isEnabled();

        boolean isHovered = NVGUtils.isHovering(mouseX, mouseY,
                (int) (getPosition().x + 90 + 200 - 14 + 24),
                (int) (getPosition().y - (float) 14 / 2),
                14, 14);

        if (isHovered != lastHovered) {
            stopWatch.reset();
        }
        int hoverAlpha = isHovered ? (int) Math.min(255, (float) stopWatch.getElapsedTime() / 200.0f * 255) : 0;
        int r = isToggled ? 255 : 150;
        int alpha = isToggled ? 255 : hoverAlpha;

        NVGUtils.drawText(setting != null ? setting.getName() : "Enabled", getPosition().x, getPosition().y, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        NVGUtils.drawRect(getPosition().x + 90 + 200 - 14 + 24, getPosition().y - 14f / 2f, 14, 14, 2, FatalityColours.PANEL);
        NVGUtils.drawCheckmark(getPosition().x + 90 + 200 - 14 + 24 + 2, getPosition().y - 14f / 2f - 2f,
                13f, 1f, new Colour(r, r, r, alpha));

        lastHovered = isHovered;
    }


    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        boolean isHovered = NVGUtils.isHovering(mouseX, mouseY,
                (int) (getPosition().x + 90 + 200 - 14 + 24),
                (int) (getPosition().y - (float) 14 / 2),
                14, 14);
        parent.getRenderer().maskList.add(new Mask((int) (getPosition().x + 90 + 200 - 14 + 24),
                (int) (getPosition().y - (float) 14 / 2),
                14, 14));
        if (isHovered && mouseButton == 0) {
            if (getSetting() != null) {
                getSetting().setValue(!getSetting().getValue());
            } else {
                getParent().getModule().toggle();
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {

    }
}