package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class StringValueComponent extends ValueComponent<StringSetting> {
    private boolean writing = false;
    private final String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_=+[]{};:'\",.<>/?\\|`~!@#$%^&*() ";
    private static StringValueComponent focusedComponent = null;
    private long lastKeyTime = 0;
    private long lastMouseTime = 0;
    private static final long KEY_DEBOUNCE_TIME = 10;
    private static final long MOUSE_DEBOUNCE_TIME = 100;

    public StringValueComponent(StringSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    @Override
    public void render(double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 100;
        float height = 12;
        float boxX = posX + 45 + 12;
        float boxY = posY - height / 2f + 0;

        Fonts.getJoseFin(14).drawString(setting.getName(), posX, posY, -1);

        boolean hovered = RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

        // todo: fade
        Color boxColor;
        if (writing) {
            boxColor = new Color(60, 60, 60);
        } else if (hovered) {
            boxColor = new Color(50, 50, 50);
        } else {
            boxColor = new Color(40, 40, 40);
        }

        RenderUtils.drawRoundedRect(boxX, boxY, width, height, 2, boxColor);

        long time = System.currentTimeMillis();
        boolean cursorVisible = writing && (time / 500 % 2 == 0);

        String text = setting.isSecure() && !writing ?  new String(new char[setting.getValue().length()]).replace('\0', '*') : setting.getValue() + (cursorVisible ? "|" : "");
        Fonts.getJoseFin(12).drawStringWithShadow(text, boxX + 4, boxY + height / 2f - 1f, Color.WHITE.getRGB());
    }

    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return;
        }

        if (currentTime - lastMouseTime < MOUSE_DEBOUNCE_TIME) {
            return;
        }

        if (clickConsumed || mouseButton != 0) return;

        float width = 100;
        float height = 12;
        float boxX = getPosition().x + 45 + 12;
        float boxY = getPosition().y - height / 2f + 0;

        boolean clickedInside = RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

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
                if(setting.getValue().isEmpty() && !setting.isAllowBlank()) {
                    setting.setValue(setting.getDefaultValue());
                }
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return;
        }
        if (releaseConsumed) return;

        consumeRelease();
    }

    // todo: key shortcuts, highlighting, etc
    @Override
    public boolean key(char typedChar, int keyCode) {
        if (!writing || focusedComponent != this) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return false;
        }

        lastKeyTime = currentTime;

        String current = setting.getValue();

        if (allowed.indexOf(typedChar) != -1 && current.length() < this.getSetting().getMaxLength()) {
            setting.setValue(current + typedChar);
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !current.isEmpty()) {
            setting.setValue(current.substring(0, current.length() - 1));
        }

        if (keyCode == 0 || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            writing = false;
            focusedComponent = null;
            if(current.isEmpty() && !setting.isAllowBlank()) {
                setting.setValue(setting.getDefaultValue());
            }
            return true;
        }
        return false;
    }

    public boolean isFocused() {
        return focusedComponent == this && writing;
    }

    public void unfocus() {
        if (focusedComponent == this) {
            writing = false;
            focusedComponent = null;
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }
}