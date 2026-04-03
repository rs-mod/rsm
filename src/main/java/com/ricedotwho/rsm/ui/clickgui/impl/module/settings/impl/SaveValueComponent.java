package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent.focusedComponent;

public class SaveValueComponent extends InputValueComponent<SaveSetting<?>> {
    private boolean pressed = false;

    public SaveValueComponent(SaveSetting<?> setting, ModuleBase parent) {
        super(setting, parent, new TextInput(setting.getDefaultFile(), 12, false));
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        if (!setting.isAllowEdits()) return;
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 140;
        float height = 24;
        float boxX = posX + 90 + 24;
        float boxY = posY - height / 2f;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, FatalityColours.TEXT, NVGUtils.JOSEFIN);
        boolean hovered = NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

        // todo: fade
        Colour boxColor;
        if (writing) {
            boxColor = new Colour(60, 60, 60);
        } else if (hovered) {
            boxColor = new Colour(50, 50, 50);
        } else {
            boxColor = new Colour(40, 40, 40);
        }

        if (!this.setting.getFileName().equals(input.getValue())) {
            input.setValue(this.setting.getFileName());
        }

        NVGUtils.drawRect(boxX, boxY, width, height, 2, boxColor);

        float x = boxX + 8;
        float y = (boxY + height / 2f) - 4.5f;

        input.render(x, y, this.writing);

        // button
        Colour buttonColour;
        if (pressed) {
            buttonColour = FatalityColours.SELECTED.darker().darker();
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX + 150, (int) boxY, 50, (int) height)) {
            buttonColour = FatalityColours.SELECTED.darker();
        } else {
            buttonColour = FatalityColours.SELECTED;
        }
        NVGUtils.drawRect(boxX + 150, boxY, 50, height, 3, buttonColour);
        float offset = Math.max(1, (50 - NVGUtils.getTextWidth("Load", 12, NVGUtils.JOSEFIN)) / 2);
        NVGUtils.drawTextShadow("Load", boxX + offset + 150, (boxY + height / 2f) - 4.5f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        if (!setting.isAllowEdits()) return;
        if (clickConsumed || mouseButton != 0) return;

        float width = 140;
        float height = 24;
        float boxX = getPosition().x + 90 + 24;
        float boxY = getPosition().y - height / 2f;

        boolean clickedInside = NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);
        boolean clickedButton = NVGUtils.isHovering(mouseX, mouseY, (int) boxX + 150, (int) boxY, 50, (int) height);

        if (clickedInside) {
            if (focusedComponent != null && focusedComponent != this) {
                focusedComponent.writing = false;
            }

            focusedComponent = this;
            this.writing = true;
            input.click((float) (mouseX - (boxX + 8)), mouseButton);
        } else if (clickedButton) {
            this.writing = false;
            focusedComponent = this;
            pressed = true;
            setting.load();
        } else {
            if (this.writing) {
                this.writing = false;
                if (focusedComponent == this) focusedComponent = null;
                if (setting.getFileName().isBlank()) {
                    setting.setFileName(setting.getDefaultFile());
                }
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        if (!setting.isAllowEdits()) return;
        if (releaseConsumed) return;
        consumeRelease();
        pressed = false;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!setting.isAllowEdits()) return false;
        if (!writing || focusedComponent != this) return false;
        boolean ret = input.charTyped(typedChar);
        setting.setFileName(input.getValue());
        setting.updateFile();
        return ret;
    }

    @Override
    public boolean keyTyped(KeyEvent event) {
        if (!setting.isAllowEdits()) return false;
        if (!writing || focusedComponent != this) return false;
        String current = setting.getFileName();
        int key = event.key();

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            writing = false;
            focusedComponent = null;
            if (current.isEmpty()) {
                setting.setFileName(setting.getDefaultFile());
            }
            return true;
        }

        boolean ret = input.keyTyped(event);
        setting.setFileName(input.getValue());
        setting.updateFile();
        return ret;
    }

    @Override
    public int getHeight() {
        return setting.isAllowEdits() ? 14 : 0;
    }
}