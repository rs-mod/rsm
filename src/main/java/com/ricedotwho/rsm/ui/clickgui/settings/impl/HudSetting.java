package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;

import java.util.function.BooleanSupplier;

public abstract class HudSetting extends DragSetting {
    private final BooleanSupplier supplier;

    public HudSetting(String name, Vector2d defaultPos, Vector2d size, BooleanSupplier supplier) {
        super(name, defaultPos, size);
        this.supplier = supplier;
    }

    public HudSetting(String name, Vector2d defaultPos, Vector2d size, float scale, BooleanSupplier supplier) {
        super(name, defaultPos, size, scale);
        this.supplier = supplier;
    }

    public void render(GuiGraphics gfx) {
        if (this.shouldRender()) {
            this.draw(gfx);
        }
    }

    protected boolean shouldRender() {
        return supplier.getAsBoolean();
    }

    protected abstract void draw(GuiGraphics gfx);
}
