package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent.focusedComponent;

public class StringValueComponent extends InputValueComponent<StringSetting> {

    public StringValueComponent(StringSetting setting, ModuleBase parent) {
        super(setting, parent, new TextInput(setting.getValue(), 12, false));
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 200;
        float height = 24;
        float boxX = posX + 90 + 24;
        float boxY = posY - height / 2f;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);
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

        if (!this.setting.getValue().equals(input.getValue())) {
            input.setValue(this.setting.getValue());
        }

        NVGUtils.drawRect(boxX, boxY, width, height, 2, boxColor);

        float x = boxX + 8;
        float y = (boxY + height / 2f) - 4.5f;

        input.render(x, y, writing);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        if (clickConsumed || mouseButton != 0) return;

        float width = 200;
        float height = 24;
        float boxX = getPosition().x + 90 + 24;
        float boxY = getPosition().y - height / 2f;

        boolean clickedInside = NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

        if (clickedInside) {
            if (focusedComponent != null && focusedComponent != this) {
                focusedComponent.writing = false;
            }

            focusedComponent = this;
            writing = true;
            input.click((float) (mouseX - (boxX + 8)), mouseButton);
            consumeClick();
        } else {
            if (writing && focusedComponent == this) {
                writing = false;
                focusedComponent = null;
                if(setting.getValue().isEmpty() && !setting.isAllowBlank()) {
                    setting.setValue(setting.getDefaultValue());
                }
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        if (releaseConsumed) return;
        consumeRelease();
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!writing || focusedComponent != this) return false;
        boolean ret = input.charTyped(typedChar);
        this.setting.setValue(input.getValue());
        return ret;
    }

    @Override
    public boolean keyTyped(KeyEvent event) {
        if (!writing || focusedComponent != this) return false;
        String current = setting.getValue();
        int key = event.key();

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            writing = false;
            focusedComponent = null;
            if (current.isEmpty() && !setting.isAllowBlank()) {
                setting.setValue(setting.getDefaultValue());
            }
            return true;
        }

        boolean ret = input.keyTyped(event);;
        this.setting.setValue(input.getValue());
        return ret;
    }

    @Override
    public int getHeight() {
        return 14;
    }
}