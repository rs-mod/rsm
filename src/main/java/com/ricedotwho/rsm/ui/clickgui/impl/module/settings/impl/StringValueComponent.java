package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.InputValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent.focusedComponent;

public class StringValueComponent extends InputValueComponent<StringSetting> {

    public StringValueComponent(StringSetting setting, ModuleBase parent) {
        super(setting, parent, new TextInput(setting.getValue(), 12, false, setting.getMaxLength(), setting.isSecure()));
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
        if (getInput().isWriting()) {
            boxColor = FatalityColours.WRITING_TEXT;
        } else if (hovered) {
            boxColor = FatalityColours.HOVERING_TEXT;
        } else {
            boxColor = FatalityColours.INPUT_TEXT;
        }

        if (!this.setting.getValue().equals(getInput().getValue())) {
            getInput().setValue(this.setting.getValue());
        }

        NVGUtils.drawRect(boxX, boxY, width, height, 2, boxColor);

        float x = boxX + 8;
        float y = (boxY + height / 2f) - 4.5f;

        getInput().render(x, y);
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
                focusedComponent.setAllNotWriting();
            }

            focusedComponent = this;
            getInput().setWriting(true);
            getInput().click((float) (mouseX - (boxX + 8)), mouseButton);
        } else {
            if (this.getInput().isWriting()) {
                getInput().setWriting(false);
                if (focusedComponent == this) focusedComponent = null;
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
        if (!getInput().isWriting() || focusedComponent != this) return false;
        boolean ret = getInput().charTyped(typedChar);
        this.setting.setValue(getInput().getValue());
        getSetting().onEdit();
        return ret;
    }

    @Override
    public boolean keyTyped(KeyEvent event) {
        if (!getInput().isWriting() || focusedComponent != this) return false;
        String current = setting.getValue();
        int key = event.key();

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            getInput().setWriting(false);
            focusedComponent = null;
            if (current.isEmpty() && !setting.isAllowBlank()) {
                setting.setValue(setting.getDefaultValue());
                getSetting().onEdit();
            }
            return true;
        }

        boolean ret = getInput().keyTyped(event);
        this.setting.setValue(getInput().getValue());
        getSetting().onEdit();
        return ret;
    }

    @Override
    public int getHeight() {
        return 14;
    }
}