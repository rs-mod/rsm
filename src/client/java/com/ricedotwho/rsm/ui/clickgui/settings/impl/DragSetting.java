package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.JsonObject;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;

@Getter
@Setter
public class DragSetting extends Setting {
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

    public void renderScaled(Runnable renderer, double contentWidth, double contentHeight) {
        double scaleX = scale.x / contentWidth;
        double scaleY = scale.y / contentHeight;
        double scaleFactor = Math.min(scaleX, scaleY);

        GL11.glPushMatrix();
        GL11.glTranslated(position.x, position.y, 0);
        GL11.glScaled(scaleFactor, scaleFactor, 1);

        renderer.run();

        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        this.setShown(getSupplier().getAsBoolean());
    }
}
