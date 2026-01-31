package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;

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
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 200;
        float rectHeight = 21f;
        float offsetY = -14 / 2.0f - 3f;
        float dropdownX = posX + 90 + 24;
        float dropdownY = posY + offsetY;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        NVGUtils.drawRect(dropdownX, dropdownY, rectWidth, rectHeight, 1, FatalityColours.PANEL);

        if (expanded) {
            Map<String, Boolean> values = getSetting().getValue();
            int dropdownHeight = (values.size()) * 18;
            NVGUtils.drawRect(dropdownX, dropdownY + rectHeight, rectWidth/* * 1.5f*/, dropdownHeight, 1, FatalityColours.PANEL);
            float offset = 0;

            for (Map.Entry<String, Boolean> value : values.entrySet()) {
                float textY = dropdownY + rectHeight + offset + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) + 1;
                boolean isHovered = NVGUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) (dropdownY + rectHeight + offset), (int) (rectWidth * 1.5f), 18);

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

                Colour finalColor = new Colour(hoverAlpha, hoverAlpha, hoverAlpha);
                NVGUtils.drawText(value.getKey(), dropdownX + 5f, textY - 9f, 12, value.getValue() ? FatalityColours.SELECTED : finalColor, NVGUtils.JOSEFIN);

                offset += 18;
            }
        }

        String enabledValues = setting.getValue().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        float textWidth = NVGUtils.getTextWidth(enabledValues, 12, NVGUtils.JOSEFIN);
        float maxTextWidth = rectWidth - 20;
        if (textWidth > maxTextWidth) {
            while (NVGUtils.getTextWidth(enabledValues + "...", 12, NVGUtils.JOSEFIN) > maxTextWidth && enabledValues.length() > 1) {
                enabledValues = enabledValues.substring(0, enabledValues.length() - 1);
            }
            enabledValues += "...";
        }
        if (enabledValues.isEmpty()) {
            enabledValues = "None";
        }
        NVGUtils.drawText(enabledValues, dropdownX + 5f, posY - 2.5f, 12, FatalityColours.UNSELECTED_TEXT, NVGUtils.JOSEFIN);
        NVGUtils.drawArrow(dropdownX + rectWidth - 15f, posY + offsetY + 6f, 10, 1, new Colour(Color.WHITE), expanded);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 200;
        float rectHeight = 21f;
        float offsetY = -14 / 2.0f - 3f;
        float dropdownX = posX + 90 + 24;
        float dropdownY = posY + offsetY;
        parent.getRenderer().maskList.add(new Mask((int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight));
        if (NVGUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight) && mouseButton == 0) {
            expanded = !expanded;
        }
        if (expanded){
            float offset = 0;
            for (Map.Entry<String, Boolean> boo /* AAH */: getSetting().getValue().entrySet()) {
                if (NVGUtils.isHovering(mouseX,mouseY, (int) dropdownX, (int) (dropdownY + rectHeight + offset), (int) (rectWidth * 1.5f), 18) && mouseButton == 0){
                    setting.set(boo.getKey(), !boo.getValue());
                    consumeClick();
                }
                offset += 18;
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {

    }
}