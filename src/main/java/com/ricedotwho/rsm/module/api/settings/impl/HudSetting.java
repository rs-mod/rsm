package com.ricedotwho.rsm.module.api.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Getter
@Setter
public abstract class HudSetting extends Setting<Vector2f> {
    private float scale;
    private Vector2f lastSize;

    public HudSetting(String name, Vector2f defaultPos, float scale = 5f, String description = "") {
        super(name, description, defaultPos);
        this.value = defaultPos;
        this.scale = scale;
    }

    @Override
    public void resetToDefault() {
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, scale);
    }

    @Override
    public void readFromJson(JsonObject obj) {
        var x = obj.get("x").getAsFloat();
        var y = obj.get("y").getAsFloat();
        this.setValue(new Vector2f(x, y));
        this.setScale(obj.get("scale").getAsFloat());
    }

    @Override
    public void writeToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("x", this.value.x);
        obj.addProperty("y", this.value.y);
        obj.addProperty("scale", this.scale);
    }

    @Override
    public String getType() {
        return "drag";
    }

    public Vector2f draw(GuiGraphicsExtractor gfx, boolean example) {
        if (!example && !getIsVisible().getAsBoolean()) return null;
        float guiScale = mc.getWindow().getGuiScale();
        gfx.push();
        if (!example) gfx.pose().scale(1.0f / guiScale);
        gfx.pose().translate((float) this.value.x, (float) this.value.y);
        gfx.pose().scale(scale, scale);
        lastSize = this.render(gfx, example).mul(scale);
        gfx.pop();
        return lastSize;
    }

    protected abstract @NotNull Vector2f render(GuiGraphicsExtractor gfx, boolean example);

    public int text(GuiGraphicsExtractor gfx, String content, int x, int y, Color color = Color.WHITE, TextAlignment align = TextAlignment.TopLeft, boolean shadow = false) {
        var xOff = align.calculateX(mc.font.width(content));
        var yOff = align.calculateY(mc.font.lineHeight);
        gfx.text(mc.font, content, (int) (x + xOff), (int) (y + yOff), color.getARGB(), shadow);
        return mc.font.width(content);
    }

    protected Vector2f dims(float x, float y = mc.font.lineHeight) {
        return new Vector2f(x, y);
    }

    protected Vector2f dims(String content) {
        return new Vector2f(mc.font.width(content), mc.font.lineHeight);
    }
}
