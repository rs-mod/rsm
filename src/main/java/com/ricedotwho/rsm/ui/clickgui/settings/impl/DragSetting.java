package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class DragSetting extends Setting implements Accessor {
    private Vector2d position;
    private Vector2d dragPos;
    private final Vector2d size;
    private float scale;

    private boolean dragging;

    public DragSetting(String name,Vector2d defaultPos, Vector2d size) {
        super(name, null, null);
        this.position = defaultPos;
        this.size = size;
        this.scale = 1;
    }

    public DragSetting(String name,Vector2d defaultPos, Vector2d size, float scale) {
        super(name, null, null);
        this.position = defaultPos;
        this.size = size;
        this.scale = scale;
    }

    public DragSetting(String name, Vector2d defaultPos, Vector2d size, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.position = defaultPos;
        this.size = size;
        this.scale = 1;
    }

    public DragSetting(String name, Vector2d defaultPos, Vector2d size, float scale, BooleanSupplier supplier) {
        super(name, supplier, null);
        this.position = defaultPos;
        this.size = size;
        this.scale = scale;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        int x = obj.get("x").getAsInt();
        int y = obj.get("y").getAsInt();
        // config converting zzz
        float scale;
        if (!obj.has("scale")) {
            double oldScalingX = obj.get("scaleX").getAsDouble();
            scale = (float) (oldScalingX / size.x());
        } else {
            scale = obj.get("scale").getAsFloat();
        }
        this.setPosition(new Vector2d(x, y));
        this.setScale(scale == 0.0 ? 1 : scale);
    }

    @Override
    public void saveToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("x", this.getPosition().x);
        obj.addProperty("y", this.getPosition().y);
        obj.addProperty("scale", this.scale);
    }

    @Override
    public String getType() {
        return "drag";
    }

    public float getScaledX() {
        return (float) (this.size.x() * scale);
    }

    public float getScaledY() {
        return (float) (this.size.y() * scale);
    }

    public void renderScaled(GuiGraphics gfx, Runnable renderer, float contentWidth, float contentHeight) {
        if (mc.player == null || mc.level == null) return;
        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            NVGUtils.scale(RSMConfig.getStandardGuiScale());
            NVGUtils.translate((float) position.x, (int) position.y);
            float scale = getScale(contentWidth, contentHeight);
            NVGUtils.scale(scale, scale);
            renderer.run();
        });
    }

    public void renderScaledGFX(GuiGraphics gfx, Runnable renderer, float contentWidth, float contentHeight) {
        renderScaledGFX(gfx, renderer, getScale(contentWidth, contentHeight));
    }

    public void renderScaledGFX(GuiGraphics gfx, Runnable renderer) {
        renderScaledGFX(gfx, renderer, this.scale);
    }

    public void renderScaledGFX(GuiGraphics gfx, Runnable renderer, float scale) {
        if (mc.player == null || mc.level == null) return;
        float guiScale = mc.getWindow().getGuiScale();

        // what is even going on
        gfx.pose().pushMatrix();

        //ts is so cooked, need to make custom fonts work with gfx rendering so we can remove this unscaling thing
        gfx.pose().scale(1.0f / guiScale, 1.0f / guiScale);
        gfx.pose().scale(RSMConfig.getStandardGuiScale());
        gfx.pose().translate((float) this.position.x, (float) this.position.y);
        gfx.pose().scale(scale, scale);
        renderer.run();
        gfx.pose().popMatrix();
    }

    public void text(GuiGraphics gfx, String content, Align align, int x, int y, Colour colour, boolean shadow) {
        int offset = (int) switch (align) {
            case CENTER -> (this.size.x / 2f) - (mc.font.width(content) / 2f);
            case RIGHT -> this.size.x - mc.font.width(content);
            default -> 0;
        };
        gfx.drawString(mc.font, content, x + offset, y, colour.getRGB(), shadow);
    }

    private float getScale(float width, float height) {
        float l = Math.max(width, height);
        return Math.round(l * this.scale) / l;
    }

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }
}
