package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

public class StringValueComponent extends ValueComponent<StringSetting> {
    private boolean writing = false;
    private final String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_=+[]{};:'\",.<>/?\\|`~!@#$%^&*() ";
    private static StringValueComponent focusedComponent = null;
    private long lastKeyTime = 0;
    private long lastCharTime = 0;
    private long lastMouseTime = 0;
    private static final long CHAR_DEBOUNCE_TIME = 10;
    private static final long KEY_DEBOUNCE_TIME = 30;
    private static final long MOUSE_DEBOUNCE_TIME = 100;

    public StringValueComponent(StringSetting setting, ModuleBase parent) {
        super(setting, parent);
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

        NVGUtils.drawRect(boxX, boxY, width, height, 2, boxColor);

        long time = System.currentTimeMillis();
        boolean cursorVisible = writing && (time / 500 % 2 == 0);

        String text = setting.isSecure() && !writing ?  new String(new char[setting.getValue().length()]).replace('\0', '*') : setting.getValue() + (cursorVisible ? "|" : "");
        NVGUtils.drawTextShadow(text, boxX + 8, (boxY + height / 2f) - 4.5f, 12, Colour.WHITE, NVGUtils.JOSEFIN);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCharTime < KEY_DEBOUNCE_TIME) {
            return;
        }

        if (currentTime - lastMouseTime < MOUSE_DEBOUNCE_TIME) {
            return;
        }

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
    public void release(double mouseX, double mouseY, int mouseButton) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCharTime < KEY_DEBOUNCE_TIME) {
            return;
        }
        if (releaseConsumed) return;

        consumeRelease();
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!writing || focusedComponent != this) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCharTime < CHAR_DEBOUNCE_TIME) {
            return false;
        }
        lastCharTime = currentTime;
        String current = setting.getValue();

        if (allowed.indexOf(typedChar) != -1 && current.length() < this.getSetting().getMaxLength()) {
            setting.setValue(current + typedChar);
        }
        return false;
    }

    @Override
    public boolean keyTyped(KeyEvent input) {
        if (!writing || focusedComponent != this) return false;
        String current = setting.getValue();
        int key = input.key();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyTime < KEY_DEBOUNCE_TIME) {
            return false;
        }
        lastKeyTime = currentTime;

        if (key == GLFW.GLFW_KEY_BACKSPACE && !current.isEmpty()) {
            setting.setValue(current.substring(0, current.length() - 1));
        }

        if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
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