package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ModeValueComponent extends ValueComponent<ModeSetting> {

    private boolean expanded;

    public ModeValueComponent(ModeSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    private final Map<String, StopWatch> hoverTimers = new HashMap<>();
    private final String lastHoveredValue = null;
    private final Map<String, Boolean> hoverStates = new HashMap<>();

    @Override
    public void render(double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 100;
        float rectHeight = 10.5f;
        float offsetY = -7 / 2.0f - 1.5f;
        float dropdownX = posX + 45 + 12;
        float dropdownY = posY + offsetY;

        Fonts.getJoseFin(14).drawString(setting.getName(), posX, posY, -1);

        RenderUtils.drawRoundedRect(dropdownX, dropdownY, rectWidth, rectHeight, 1, FatalityColors.PANEL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (expanded) {
            ArrayList<String> values = getSetting().getValues();
            int dropdownHeight = (values.size()) * 9;
            RenderUtils.drawRoundedRect(dropdownX, dropdownY + rectHeight, rectWidth/* * 1.5f*/, dropdownHeight, 1, FatalityColors.PANEL);
            float offset = 0;

            for (String value : values) {
                float textY = dropdownY + rectHeight + offset + Fonts.getJoseFin(12).getHeight(value) + 1;
                boolean isHovered = RenderUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) (dropdownY + rectHeight + offset), (int) (rectWidth * 1.5f), 9);

                hoverTimers.putIfAbsent(value, new StopWatch());
                hoverStates.putIfAbsent(value, false);
                StopWatch valueTimer = hoverTimers.get(value);

                if (isHovered) {
                    if (!hoverStates.get(value)) {
                        valueTimer.reset();
                    }
                    hoverStates.put(value, true);
                } else {
                    hoverStates.put(value, false);
                }

                int hoverAlpha;
                if (hoverStates.get(value)) {
                    hoverAlpha = 255;
                } else {
                    hoverAlpha = Math.max(150, 255 - (int) (valueTimer.getElapsedTime() / 200.0f * 105));
                }

                Color finalColor = new Color(hoverAlpha, hoverAlpha, hoverAlpha);
                Fonts.getJoseFin(12).drawString(value, dropdownX + 2.5f, textY,
                        value.equals(setting.getValue()) ? FatalityColors.SELECTED.getRGB() : finalColor.getRGB());

                offset += 9;
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        Fonts.getJoseFin(12).drawString(setting.getValue(), dropdownX + 2.5f, posY, FatalityColors.UNSELECTED_TEXT.getRGB());
        RenderUtils.drawArrow(dropdownX + rectWidth - 7.5f, posY + offsetY + 3.0f, 5, 2, Color.WHITE, expanded);
    }

    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 100;
        float rectHeight = 10.5f;
        float offsetY = -7 / 2.0f - 1.5f;
        float dropdownX = posX + 45 + 12;
        float dropdownY = posY + offsetY;
        parent.getRenderer().maskList.add(new Mask((int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight));
        if (RenderUtils.isHovering(mouseX, mouseY, (int) (getPosition().x + 45 + 12), (int) ((int) (getPosition().y - (double) 7 / 2) - 1.5f), (int) rectWidth, (int) rectHeight) && mouseButton == 0) {
            expanded = !expanded;
        }
        if (expanded){
            float offset = 0;
            for (String s : getSetting().getValues()) {
                if(RenderUtils.isHovering(mouseX,mouseY, (int) dropdownX, (int) (dropdownY + rectHeight + offset), (int) (rectWidth * 1.5f), 9) && mouseButton == 0){
                    setting.setValue(s);
                    expanded = false;
                }
                offset += 9;
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {

    }
}