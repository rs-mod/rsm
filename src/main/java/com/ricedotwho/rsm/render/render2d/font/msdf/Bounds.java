package com.ricedotwho.rsm.render.render2d.font.msdf;

import com.google.gson.JsonObject;

public record Bounds(float left, float bottom, float right, float top) {
    public Bounds(JsonObject obj) {
        this(
                obj.get("left").getAsFloat(),
                obj.get("bottom").getAsFloat(),
                obj.get("right").getAsFloat(),
                obj.get("top").getAsFloat()
        );
    }
}
