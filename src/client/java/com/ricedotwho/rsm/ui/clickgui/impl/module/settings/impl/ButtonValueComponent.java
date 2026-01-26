package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;


import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.font.TTFFontRenderer;
import com.ricedotwho.rsm.utils.render.RenderUtils;

import java.awt.*;

public class ButtonValueComponent extends ValueComponent<ButtonSetting> {
    private boolean pressed = false;
    private static ButtonValueComponent focusedComponent = null;
    private long lastMouseTime = 0;
    private static final long MOUSE_DEBOUNCE_TIME = 100;

    public ButtonValueComponent(ButtonSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 50;
        float height = 10;
        float boxX = posX + 95 + 12;
        float boxY = posY - height / 2f + 0;

        Fonts.getJoseFin(14).drawString(setting.getName(), posX, posY, -1);

        // todo: fade
        Color boxColor;
        if (pressed) {
            boxColor = FatalityColors.SELECTED.darker().darker();
        } else if (RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height)) {
            boxColor = FatalityColors.SELECTED.darker();
        } else {
            boxColor = FatalityColors.SELECTED;
        }

        RenderUtils.drawRoundedRect(boxX, boxY, width, height, 3, boxColor);
        TTFFontRenderer font = Fonts.getJoseFin(12);
        String text = setting.getValue();
        float offset = Math.max(1, (width - font.getWidth(text)) / 2);
        font.drawStringWithShadow(text , boxX + offset, boxY + height / 2f - 1f, Color.WHITE.getRGB());
    }

    @Override
    public void click(int mouseX, int mouseY, float mouseButton) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMouseTime < MOUSE_DEBOUNCE_TIME) {
            return;
        }

        if (clickConsumed || mouseButton != 0) return;

        float width = 50;
        float height = 12;
        float boxX = getPosition().x + 95 + 12;
        float boxY = getPosition().y - height / 2f + 0;

        boolean clickedInside = RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

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
    public void release(int mouseX, int mouseY, float mouseButton) {
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