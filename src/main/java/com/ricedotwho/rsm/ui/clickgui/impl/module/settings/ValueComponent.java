package com.ricedotwho.rsm.ui.clickgui.impl.module.settings;

import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2f;

@Getter
public abstract class ValueComponent<T extends Setting<?>> {
    protected final T setting;
    protected ModuleComponent parent;

    @Setter
    protected Vector2f position;

    @Setter
    protected int height;

    @Getter
    protected boolean clickConsumed = false;
    protected boolean releaseConsumed = false;
    protected long lastClickTime = 0;

    public ValueComponent(T setting, ModuleComponent parent) {
        this.setting = setting;
        this.parent = parent;
    }

    public abstract void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks);

    public abstract void click(double mouseX, double mouseY, int mouseButton);

    public abstract void release(double mouseX, double mouseY, int mouseButton);

    public boolean key(char typedChar, int keyCode) {
        return false;
    }

    public void consumeClick() {
        clickConsumed = true;
        lastClickTime = System.currentTimeMillis();
    }

    public void resetClick() {
        clickConsumed = false;
        releaseConsumed = false;
    }

    public void consumeRelease() {
        releaseConsumed = true;
    }

    protected boolean shouldIgnoreClick() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastClickTime < 100;
    }
}