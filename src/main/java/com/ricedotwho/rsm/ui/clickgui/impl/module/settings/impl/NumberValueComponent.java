package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.BinaryOperator;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent.focusedComponent;

public class NumberValueComponent extends InputValueComponent<NumberSetting> {
    private boolean dragging = false;
    private float lastWidth = 0;
    private long lastMs = System.currentTimeMillis();

    private boolean writing = false;

    public NumberValueComponent(NumberSetting setting, ModuleBase parent) {
        super(setting, parent, new TextInput(setting.getValueAsString(), 12, true));
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 140;
        float inputWidth = 50;
        float rectHeight = 16;
        float offsetY = -14 / 2.0f - 1f;
        float dropdownX = posX + 90 + 24;
        float dropdownY = posY + offsetY;

        float inputX = dropdownX + 150;
        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);
        NVGUtils.drawRect(dropdownX, dropdownY, rectWidth, rectHeight, 1, FatalityColours.PANEL);

        double value = setting.getValue().doubleValue(), min = setting.getMin().doubleValue(), max = setting.getMax().doubleValue();

        float percent = (float) ((value - min) / (max - min));
        float targetSliderWidth = percent * (rectWidth - 4);

        if (lastWidth == 0) lastWidth = targetSliderWidth;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastMs) / 1000.0f;
        lastMs = currentTime;

        float smoothingSpeed = 12.0f;
        lastWidth += (targetSliderWidth - lastWidth) * Math.min(1.0f, deltaTime * smoothingSpeed);

        if (Math.abs(lastWidth - targetSliderWidth) < 0.5f) {
            lastWidth = targetSliderWidth;
        }

        NVGUtils.drawRect(dropdownX + 2, dropdownY + 2, lastWidth, rectHeight - 4, 1, FatalityColours.SELECTED);

        String valueString = this.setting.getValueAsString();

        NVGUtils.drawTextShadow(valueString + setting.getUnit(), dropdownX + rectWidth / 2, posY - 4.5f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

        if (dragging) {
            float mouseOffset = (float) (mouseX - dropdownX);
            double newPercent = Math.max(0, Math.min(1, mouseOffset / (rectWidth - 4)));
            double newValue = min + newPercent * (max - min);

            double increment = setting.getIncrement().doubleValue();

            if (increment != 0) {
                newValue = round(newValue, increment);
            }
            setting.setValue(newValue);
        }

        if (!writing) input.setValue(this.setting.getValueAsString());

        // todo: fade
        Colour boxColor;
        if (writing) {
            boxColor = new Colour(60, 60, 60);
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) inputX, (int) dropdownY, inputWidth, (int) rectHeight)) {
            boxColor = new Colour(50, 50, 50);
        } else {
            boxColor = new Colour(40, 40, 40);
        }
        NVGUtils.drawRect(inputX, dropdownY, inputWidth, rectHeight, 2f, boxColor);

        input.render(inputX + 8, (dropdownY + rectHeight / 2f) - 4.5f, writing);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        float posX = getPosition().x;
        float posY = getPosition().y;
        float rectWidth = 140;
        float rectHeight = 16;
        float offsetY = -14 / 2.0f - 1f;
        float dropdownX = posX + 90 + 24;
        float dropdownY = posY + offsetY;
        RSM.getInstance().getConfigGui().maskList.add(new Mask((int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight));
        if (NVGUtils.isHovering(mouseX, mouseY, (int) dropdownX, (int) dropdownY, (int) rectWidth, (int) rectHeight) && mouseButton == 0) {
            dragging = true;
            writing = false;
        }

        if (clickConsumed || mouseButton != 0) return;

        float inputX = dropdownX + 150;
        boolean clickedInside = NVGUtils.isHovering(mouseX, mouseY, (int) inputX, (int) dropdownY, 50, (int) rectHeight);

        if (clickedInside) {
            if (focusedComponent != null && focusedComponent != this) {
                focusedComponent.writing = false;
            }

            focusedComponent = this;
            writing = true;
            input.click((float) (mouseX - (inputX + 8)), mouseButton);
            consumeClick();
        } else {
            if (writing && focusedComponent == this) {
                writing = false;
                focusedComponent = null;
                if (input.getValue().isEmpty()) {
                    setting.setValue(setting.getDefaultValue());
                    input.setValue(setting.getValue().toString());
                } else if (NumberUtils.isCompactNumber(input.getValue())) {
                    setting.setValue(NumberUtils.parseCompact(input.getValue()));
                    input.setValue(setting.getValue().toString());
                }
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!writing || focusedComponent != this) return false;
        boolean ret = input.charTyped(typedChar);

        if (!input.getValue().isEmpty() && NumberUtils.isCompactNumber(input.getValue())) {
            setting.setValue(NumberUtils.parseCompact(input.getValue()));
        }
        return ret;
    }

    @Override
    public boolean keyTyped(KeyEvent event) {
        if (!writing || focusedComponent != this) return false;
        String current = input.getValue();
        int key = event.key();

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            writing = false;
            focusedComponent = null;
            if(current.isEmpty()) {
                setting.setValue(setting.getDefaultValue());
                input.setValue(setting.getValue().toString());
            } else if (NumberUtils.isCompactNumber(input.getValue())) {
                setting.setValue(NumberUtils.parseCompact(input.getValue()));
                input.setValue(setting.getValue().toString());
            }
            return true;

        }

        boolean ret = input.keyTyped(event);

        if (!input.getValue().isEmpty() && NumberUtils.isCompactNumber(input.getValue())) {
            setting.setValue(NumberUtils.parseCompact(input.getValue()));
        }

        return ret;
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        dragging = false;
        if (releaseConsumed) return;
        consumeRelease();
    }

    private static double round(final double value, final double places) {
        if (places < 0) throw new IllegalArgumentException();
        final double precision = 1 / places;
        return Math.round(value * precision) / precision;
    }
}