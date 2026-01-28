package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.val;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class NumberValueComponent extends ValueComponent<NumberSetting> {
    private boolean dragging = false;

    public NumberValueComponent(NumberSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }
    private float lastWidth = 0;
    private long lastMs = System.currentTimeMillis();

    private boolean writing = false;
    private static NumberValueComponent focusedComponent = null;
    private long lastKeyTime = 0;
    private long lastMouseTime = 0;
    private static final long KEY_DEBOUNCE_TIME = 10;
    private static final long MOUSE_DEBOUNCE_TIME = 100;

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 70;//49.5f;
        float inputWidth = 25;
        float rectHeight = 8;
        float offsetY = -7 / 2.0f - 0.5f;
        float dropdownX = posX + 45 + 12;
        float dropdownY = posY + offsetY;

        float inputX = dropdownX + 75;
        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);
        NVGUtils.drawRect(dropdownX, dropdownY, rectWidth, rectHeight, 1, FatalityColors.PANEL);

        float percent = (float) ((setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()));
        float targetSliderWidth = percent * (rectWidth - 2);

        if (lastWidth == 0) lastWidth = targetSliderWidth;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastMs) / 1000.0f;
        lastMs = currentTime;

        float smoothingSpeed = 12.0f;
        lastWidth += (targetSliderWidth - lastWidth) * Math.min(1.0f, deltaTime * smoothingSpeed);

        if (Math.abs(lastWidth - targetSliderWidth) < 0.5f) {
            lastWidth = targetSliderWidth;
        }

        NVGUtils.drawRect(dropdownX + 1, dropdownY + 1, lastWidth, rectHeight - 2, 1, FatalityColors.SELECTED);

        String valueString = this.setting.getValueAsString();

        NVGUtils.drawTextShadow(valueString + setting.getUnit(), dropdownX + rectWidth / 2, posY - 0.05f, 12, FatalityColors.TEXT, NVGUtils.JOSEFIN);

        if (dragging) {
            float mouseOffset = (float) (mouseX - dropdownX);
            double newPercent = Math.max(0, Math.min(1, mouseOffset / (rectWidth - 2)));
            double newValue = setting.getMin() + newPercent * (setting.getMax() - setting.getMin());

            if (setting.getIncrement() != 0) {
                newValue = round(newValue, (float) setting.getIncrement());
            }
            setting.setValue(newValue);
            setting.setStringValue(this.setting.getValueAsString());
        }

        // todo: fade
        Colour boxColor;
        if (writing) {
            boxColor = new Colour(60, 60, 60);
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) inputX, (int) dropdownY, 25, (int) rectHeight)) {
            boxColor = new Colour(50, 50, 50);
        } else {
            boxColor = new Colour(40, 40, 40);
        }
        NVGUtils.drawRect(inputX, dropdownY, inputWidth, rectHeight, 2f, boxColor);

        long time = System.currentTimeMillis();
        boolean cursorVisible = writing && (time / 500 % 2 == 0);

        String val = setting.getStringValue();

        float textWidth = NVGUtils.getTextWidth(val, 12, NVGUtils.JOSEFIN);
        float maxTextWidth = inputWidth - 10;
        String cursor = (cursorVisible ? "|" : "");
        if (textWidth > maxTextWidth) {
            while (NVGUtils.getTextWidth(val + cursor, 12, NVGUtils.JOSEFIN) > maxTextWidth && val.length() > 1) {
                val = val.substring(0, val.length() - 1);
            }
        }
        NVGUtils.drawTextShadow(val + cursor, inputX + 4, dropdownY + rectHeight / 2f - 1f, 12, Colour.WHITE, NVGUtils.JOSEFIN);
    }

    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 70;
        float rectHeight = 8;
        float offsetY = -7 / 2.0f - 0.5f;
        float dropdownX = posX + 45 + 12;
        float dropdownY = posY + offsetY;
        this.getParent().getRenderer().maskList.add(new Mask((int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight));
        if (NVGUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight) && mouseButton == 0) {
            dragging = true;
            writing = false;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return;
        }

        if (currentTime - lastMouseTime < MOUSE_DEBOUNCE_TIME) {
            return;
        }

        if (clickConsumed || mouseButton != 0) return;

        float inputX = dropdownX + 75;
        boolean clickedInside = NVGUtils.isHovering(mouseX, mouseY, (int) inputX, (int) dropdownY, 25, (int) rectHeight);

        if (clickedInside) {
            if (focusedComponent != null && focusedComponent != this) {
                focusedComponent.writing = false;
            }

            focusedComponent = this;
            writing = true;
            lastMouseTime = currentTime;
            consumeClick();
        } else {
            if (writing && focusedComponent == this) {
                writing = false;
                focusedComponent = null;
                if (setting.getStringValue().isEmpty()) {
                    setting.setValue(setting.getDefaultValue());
                    setting.setStringValue(setting.getValue().toString());
                } else if (NumberUtils.isCompactNumber(setting.getStringValue())) {
                    setting.setValue(NumberUtils.parseCompact(setting.getStringValue()));
                    setting.setStringValue(setting.getValue().toString());
                }
            }
        }
    }

    @Override
    public boolean key(char typedChar, int keyCode) {
        if (!writing || focusedComponent != this) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return false;
        }

        lastKeyTime = currentTime;

        String current = setting.getStringValue();

        String allowed = "0123456789.kmbKMB";
        int maxLength = String.valueOf(setting.getMax()).length();
        if (allowed.indexOf(typedChar) != -1 && current.length() < (current.contains(".") ? maxLength + 1 : maxLength)) {
            setting.setStringValue(current + typedChar);
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !current.isEmpty()) {
            setting.setStringValue(current.substring(0, current.length() - 1));
        }

        if (keyCode == 0 || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            writing = false;
            focusedComponent = null;
            if(current.isEmpty()) {
                setting.setValue(setting.getDefaultValue());
                setting.setStringValue(setting.getValue().toString());
            } else if (NumberUtils.isCompactNumber(setting.getStringValue())) {
                setting.setValue(NumberUtils.parseCompact(setting.getStringValue()));
                setting.setStringValue(setting.getValue().toString());
            }
            return true;
        }
        if (!setting.getStringValue().isEmpty() && NumberUtils.isCompactNumber(setting.getStringValue())) {
            setting.setValue(NumberUtils.parseCompact(setting.getStringValue()));
        }
        return false;
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {
        dragging = false;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return;
        }
        if (releaseConsumed) return;

        consumeRelease();
    }

    private static double round(final double value, final float places) {
        if (places < 0) throw new IllegalArgumentException();
        final double precision = 1 / places;
        return Math.round(value * precision) / precision;
    }
}