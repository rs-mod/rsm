package com.ricedotwho.rsm.module.api.settings.impl;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2d;

import java.util.function.BooleanSupplier;

public abstract class HudSetting extends DragSetting {
    private BooleanSupplier shouldRender;

    public HudSetting(String name, Vector2d defaultPos, Vector2d size, float scale = 1f, String description = "") {
        super(name, defaultPos, size, scale, description);
    }

    public HudSetting shouldRender(BooleanSupplier shouldRender) {
        this.shouldRender = shouldRender;
        return this;
    }


    public void render(GuiGraphicsExtractor gfx) {
        if (this.shouldRender()) {
            this.draw(gfx);
        }
    }

    protected boolean shouldRender() {
        return shouldRender.getAsBoolean();
    }

    protected abstract void draw(GuiGraphicsExtractor gfx);
}
