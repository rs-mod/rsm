package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.font.Fonts;
import com.ricedotwho.rsm.utils.font.TTFFontRenderer;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class KeybindValueComponent extends ValueComponent<KeybindSetting> {
    private boolean waiting = false;
    private static KeybindValueComponent focusedComponent = null;

    public KeybindValueComponent(KeybindSetting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    /**
     * schizo impl for toggle keybind
     * Why the fuck is this declared twice
     */
    public KeybindValueComponent(ModuleComponent moduleComponent) {
        super(new KeybindSetting("Toggle Keybind", moduleComponent.getModule().getKeybind(), () -> moduleComponent.getModule().onKeyToggle()), moduleComponent);
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 50;
        float height = 12;
        float boxX = posX + 95 + 12;
        float boxY = posY - height / 2f + 0;

        Fonts.getJoseFin(14).drawString(setting.getName(), posX, posY, -1);

        // todo: fade
        Color boxColor;
        if (waiting) {
            boxColor = new Color(60, 60, 60);
        } else if (RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height)) {
            boxColor = new Color(50, 50, 50);
        } else {
            boxColor = new Color(40, 40, 40);
        }

        RenderUtils.drawRoundedRect(gfx, boxX, boxY, width, height, 2, boxColor);

        String text =  (waiting || setting.getValue() == null ? "..." : setting.getValue().getDisplay());

        TTFFontRenderer font = Fonts.getJoseFin(12);
        float offset = Math.max(1, (width - font.getWidth(text)) / 2);
        font.drawStringWithShadow(text, boxX + offset, boxY + height / 2f - 1f, Color.WHITE.getRGB());
    }

    @Override
    public void click(double mouseX, double mouseY, float mouseButton) {

        float width = 50;;
        float height = 12;
        float boxX = getPosition().x + 95 + 12;
        float boxY = getPosition().y - height / 2f + 0;

        boolean clickedInside = RenderUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

        if (this.waiting && focusedComponent == this) {
            this.waiting = false;
            focusedComponent = null;
            setting.getValue().setKeyBind(InputConstants.Type.MOUSE.getOrCreate((int) mouseButton));
            return;
        }

        if (clickedInside) {
            if (focusedComponent != null && focusedComponent != this) {
                focusedComponent.waiting = false;
            }

            focusedComponent = this;
            this.waiting = true;
        }
    }

    @Override
    public void release(double mouseX, double mouseY, float mouseButton) {

    }

    @Override
    public boolean key(char typedChar, int keyCode) {
        if(!this.waiting || focusedComponent != this) return false;

        Keybind current = setting.getValue();
        this.waiting = false;
        focusedComponent = null;

        if (keyCode == 0 || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            current.setKeyBind(InputConstants.Type.KEYSYM.getOrCreate(0));
            focusedComponent = null;
            return true;
        }

        current.setKeyBind(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
        return false;
    }

    @Override
    public int getHeight() {
        return 14;
    }
}