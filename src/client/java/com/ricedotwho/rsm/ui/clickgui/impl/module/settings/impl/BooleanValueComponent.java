package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.RenderUtils;

import java.awt.*;

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
    public void render(double mouseX, double mouseY, float partialTicks) {
        boolean isToggled = getSetting() != null ? getSetting().getValue() : getParent().getModule().isEnabled();

        boolean isHovered = RenderUtils.isHovering(mouseX, mouseY,
                (int) (getPosition().x + 45 + 100 - 7 + 12),
                (int) (getPosition().y - (float) 7 / 2),
                7, 7);

        if (isHovered != lastHovered) {
            stopWatch.reset();
        }
        int hoverAlpha = isHovered ? (int) Math.min(255, (float) stopWatch.getElapsedTime() / 200.0f * 255) : 0;
        int r = isToggled ? 255 : 150;
        int alpha = isToggled ? 255 : hoverAlpha;

        Fonts.getJoseFin(14).drawString(setting != null ? setting.getName() : "Enabled",
                getPosition().x, getPosition().y, -1);

        RenderUtils.drawRoundedRect(getPosition().x + 45 + 100 - 7 + 12, getPosition().y - (double) 7 / 2, 7, 7, 1, FatalityColors.PANEL);
        RenderUtils.drawCheckmark(getPosition().x + 45 + 100 - 7 + 12 + 1, getPosition().y - (float) 7 / 2 - 1f,
                6.5f, 2.5f, new Color(r, r, r, alpha));

        lastHovered = isHovered;
    }


    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {
        boolean isHovered = RenderUtils.isHovering(mouseX, mouseY,
                (int) (getPosition().x + 45 + 100 - 7 + 12),
                (int) (getPosition().y - (float) 7 / 2),
                7, 7);
        parent.getRenderer().maskList.add(new Mask((int) (getPosition().x + 45 + 100 - 7 + 12),
                (int) (getPosition().y - (float) 7 / 2),
                7, 7));
        if (isHovered && mouseButton == 0) {
            if (getSetting() != null) {
                getSetting().setValue(!getSetting().getValue());
            } else {
                getParent().getModule().toggle();
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {

    }
}