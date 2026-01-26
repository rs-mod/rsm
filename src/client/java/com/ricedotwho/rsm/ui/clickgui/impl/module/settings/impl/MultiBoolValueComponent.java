package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MultiBoolValueComponent extends ValueComponent<MultiBoolSetting> {

    private boolean expanded;

    public MultiBoolValueComponent(MultiBoolSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    private final Map<String, StopWatch> hoverTimers = new HashMap<>();
    private final Map<String, Boolean> hoverStates = new HashMap<>();

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 100;
        float rectHeight = 10.5f;
        float offsetY = -7 / 2.0f - 1.5f;
        float dropdownX = posX + 45 + 12;
        float dropdownY = posY + offsetY;

        Fonts.getJoseFin(14).drawString(setting.getName(), posX, posY, -1);

        RenderUtils.drawRoundedRect(dropdownX, dropdownY, rectWidth, rectHeight, 1, FatalityColors.PANEL);

        if (expanded) {
            Map<String, Boolean> values = getSetting().getValue();
            int dropdownHeight = (values.size()) * 9;
            RenderUtils.drawRoundedRect(dropdownX, dropdownY + rectHeight, rectWidth/* * 1.5f*/, dropdownHeight, 1, FatalityColors.PANEL);
            float offset = 0;

            for (Map.Entry<String, Boolean> value : values.entrySet()) {
                float textY = dropdownY + rectHeight + offset + Fonts.getJoseFin(12).getHeight(value.getKey()) + 1;
                boolean isHovered = RenderUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) (dropdownY + rectHeight + offset), (int) (rectWidth * 1.5f), 9);

                hoverTimers.putIfAbsent(value.getKey(), new StopWatch());
                hoverStates.putIfAbsent(value.getKey(), false);
                StopWatch valueTimer = hoverTimers.get(value.getKey());

                if (isHovered) {
                    if (!hoverStates.get(value.getKey())) {
                        valueTimer.reset();
                    }
                    hoverStates.put(value.getKey(), true);
                } else {
                    hoverStates.put(value.getKey(), false);
                }

                int hoverAlpha;
                if (hoverStates.get(value.getKey())) {
                    hoverAlpha = 255;
                } else {
                    hoverAlpha = Math.max(150, 255 - (int) (valueTimer.getElapsedTime() / 200.0f * 105));
                }

                Color finalColor = new Color(hoverAlpha, hoverAlpha, hoverAlpha);
                Fonts.getJoseFin(12).drawString(value.getKey(), dropdownX + 2.5f, textY,
                        value.getValue() ? FatalityColors.SELECTED.getRGB() : finalColor.getRGB());

                offset += 9;
            }
        }

        String enabledValues = setting.getValue().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        float textWidth = Fonts.getJoseFin(12).getWidth(enabledValues);
        float maxTextWidth = rectWidth - 10;
        if (textWidth > maxTextWidth) {
            while (Fonts.getJoseFin(12).getWidth(enabledValues + "...") > maxTextWidth && enabledValues.length() > 1) {
                enabledValues = enabledValues.substring(0, enabledValues.length() - 1);
            }
            enabledValues += "...";
        }
        if (enabledValues.isEmpty()) {
            enabledValues = "None";
        }
        Fonts.getJoseFin(12).drawString(enabledValues, dropdownX + 2.5f, posY, FatalityColors.UNSELECTED_TEXT.getRGB());
        RenderUtils.drawArrow(dropdownX + rectWidth - 7.5f, posY + offsetY + 3.0f, 5, 2, Color.WHITE, expanded);
    }

    @Override
    public void click(int mouseX, int mouseY, float mouseButton) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 100;
        float rectHeight = 10.5f;
        float offsetY = -7 / 2.0f - 1.5f;
        float dropdownX = posX + 45 + 12;
        float dropdownY = posY + offsetY;
        parent.getRenderer().maskList.add(new Mask((int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight));
        if (RenderUtils.isHovering(mouseX, mouseY, (int) (getPosition().x + 45 + 22), (int) ((int) (getPosition().y - (double) 7 / 2) - 1.5f), (int) rectWidth, (int) rectHeight) && mouseButton == 0) {
            expanded = !expanded;
        }
        if (expanded){
            float offset = 0;
            for (Map.Entry<String, Boolean> skibidi: getSetting().getValue().entrySet()) {
                if (RenderUtils.isHovering(mouseX,mouseY, (int) dropdownX, (int) (dropdownY + rectHeight + offset), (int) (rectWidth * 1.5f), 9) && mouseButton == 0){
                    setting.set(skibidi.getKey(), !skibidi.getValue());
                }
                offset += 9;
            }
        }
    }

    @Override
    public void release(int mouseX, int mouseY, float mouseButton) {

    }
}