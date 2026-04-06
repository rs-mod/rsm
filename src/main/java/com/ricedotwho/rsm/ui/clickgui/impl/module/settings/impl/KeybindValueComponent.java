package com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.ModuleBase;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;

public class KeybindValueComponent extends ValueComponent<KeybindSetting> {
    private boolean waiting = false;
    private static KeybindValueComponent focusedComponent = null;

    public KeybindValueComponent(KeybindSetting setting, ModuleBase parent) {
        super(setting, parent);
    }

    /**
     * schizo impl for toggle keybind
     * Why the fuck is this declared twice
     */
    public KeybindValueComponent(ModuleBase base) {
        super(new KeybindSetting("rsm.module.toggle_keybind.name", base.getKeybind(), base::onKeyToggle), base);
    }

    @Override
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        float posX = getPosition().x;
        float posY = getPosition().y;

        float width = 100;
        float height = 24;
        float boxX = posX + 190 + 24;
        float boxY = posY - height / 2f;

        NVGUtils.drawText(this.getName(), posX, posY, 14, Colour.WHITE, NVGUtils.JOSEFIN);

        // todo: fade
        Colour boxColor;
        if (waiting) {
            boxColor = FatalityColours.WRITING_TEXT;
        } else if (NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height)) {
            boxColor = FatalityColours.HOVERING_TEXT;
        } else {
            boxColor = FatalityColours.INPUT_TEXT;
        }

        NVGUtils.drawRect(boxX, boxY, width, height, 2f, boxColor);

        String text =  (waiting || setting.getValue() == null ? "..." : setting.getValue().getDisplay());

        float offset = Math.max(1, (width - NVGUtils.getTextWidth(text, 12, NVGUtils.JOSEFIN)) / 2);
        NVGUtils.drawTextShadow(text, boxX + offset, (boxY + height / 2f) - 4.5f, 12, Colour.WHITE, NVGUtils.JOSEFIN);
    }

    @Override
    public void click(double mouseX, double mouseY, int mouseButton) {

        float width = 100;
        float height = 24;
        float boxX = getPosition().x + 190 + 24;
        float boxY = getPosition().y - height / 2f;

        boolean clickedInside = NVGUtils.isHovering(mouseX, mouseY, (int) boxX, (int) boxY, (int) width, (int) height);

        if (this.waiting && focusedComponent == this) {
            this.waiting = false;
            focusedComponent = null;
            setting.getValue().setKeyBind(InputConstants.Type.MOUSE.getOrCreate(mouseButton));
            getSetting().onEdit();
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
    public void release(double mouseX, double mouseY, int mouseButton) {

    }

    @Override
    public boolean keyTyped(KeyEvent input) {
        if (!this.waiting || focusedComponent != this) return false;

        InputConstants.Key key = InputConstants.getKey(input);

        Keybind current = setting.getValue();
        this.waiting = false;
        focusedComponent = null;

        if (key.getValue() == 0 || key.getValue() == InputConstants.KEY_ESCAPE) {
            current.setKeyBind(InputConstants.UNKNOWN);
            focusedComponent = null;
            return true;
        }

        current.setKeyBind(key);
        getSetting().onEdit();
        return false;
    }

    @Override
    public int getHeight() {
        return 28;
    }
}