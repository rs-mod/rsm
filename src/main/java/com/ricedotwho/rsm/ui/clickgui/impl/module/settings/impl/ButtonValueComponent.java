package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;

public class ButtonValueComponent extends ValueComponent<ButtonSetting> {
    private boolean pressed = false;
    private static ButtonValueComponent focusedComponent = null;
    private long lastMouseTime = 0;
    private static final long MOUSE_DEBOUNCE_TIME = 100;

    public ButtonValueComponent(ButtonSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 100;
        float height = 20;
        float boxX = posX + 190 + 24;
        float boxY = posY - height / 2f;

        NVGUtils.drawText(setting.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        // todo: fade
        Colour boxColor;
        if (pressed) {
            boxColor = FatalityColours.SELECTED.darker().darker();
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height)) {
            boxColor = FatalityColours.SELECTED.darker();
        } else {
            boxColor = FatalityColours.SELECTED;
        }

        NVGUtils.drawRect(boxX, boxY, width, height, 3, boxColor);
        String text = setting.getValue();
        float offset = Math.max(1, (width - NVGUtils.getTextWidth(text, 12, NVGUtils.JOSEFIN)) / 2);
        NVGUtils.drawTextShadow(text, boxX + offset, (boxY + height / 2f) - 4.5f, 12, Colour.WHITE, NVGUtils.JOSEFIN);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMouseTime < MOUSE_DEBOUNCE_TIME) {
            return;
        }

        if (clickConsumed || mouseButton != 0) return;

        float posX = getPosition().x;
        float posY = getPosition().y;
        float width = 100;
        float height = 20;
        float boxX = posX + 190 + 24;
        float boxY = posY - height / 2f;

        boolean clickedInside = NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

        if (clickedInside) {
            if (focusedComponent != null && focusedComponent != this) {
                focusedComponent.pressed = false;
            }

            focusedComponent = this;
            pressed = true;
            lastMouseTime = currentTime;
            consumeClick();

            if(setting.getAction() != null) setting.getAction().run();
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int mouseButton) {
        if(focusedComponent != null && focusedComponent.pressed) {
            focusedComponent.pressed = false;
        }
        focusedComponent = this;
        this.pressed = false;
    }

    @Override
    public int getHeight() {
        return 14;
    }
}