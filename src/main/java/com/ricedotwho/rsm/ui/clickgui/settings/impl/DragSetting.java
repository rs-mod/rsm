package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;

@Getter
@Setter
public class DragSetting extends Setting implements Accessor {
    private Vector2d position;
    private Vector2d dragPos;
    private Vector2d scale;
    private boolean dragging;
    public DragSetting(String name,Vector2d defaultPos, Vector2d defaultScale) {
        super(name, () -> false);
        this.position = defaultPos;
        this.scale = defaultScale;
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        int x = obj.get("x").getAsInt();
        int y = obj.get("y").getAsInt();
        double scaleX = obj.get("scaleX").getAsDouble();
        double scaleY = obj.get("scaleY").getAsDouble();
        this.setPosition(new Vector2d(x, y));
        this.setScale(new Vector2d(scaleX, scaleY));
    }

    public void renderScaled(GuiGraphics gfx, Runnable renderer, float contentWidth, float contentHeight) {
        if (mc.player == null || mc.level == null) return;
        float scaleX = (float) (scale.x / contentWidth);
        float scaleY = (float) (scale.y / contentHeight);
        float scaleFactor = Math.min(scaleX, scaleY);

        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            NVGUtils.scale(RSMConfig.getStandardGuiScale());
            NVGUtils.translate((float) position.x, (float) position.y);
            NVGUtils.scale(scaleFactor, scaleFactor);
            renderer.run();
        });
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
