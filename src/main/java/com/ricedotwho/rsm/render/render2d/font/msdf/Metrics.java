package com.ricedotwho.rsm.render.render2d.font.msdf;

import com.google.gson.JsonObject;

public record Metrics(
            int emSize,
            int lineHeight,
            float ascender,
            float descender,
            float underlineY,
            float underlineThickness
) {
    public Metrics(JsonObject obj) {
        this(
                obj.get("emSize").getAsInt(),
                obj.get("lineHeight").getAsInt(),
                obj.get("ascender").getAsFloat(),
                obj.get("descender").getAsFloat(),
                obj.get("underlineY").getAsFloat(),
                obj.get("underlineThickness").getAsFloat()
        );
    }
}
