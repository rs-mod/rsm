package com.ricedotwho.rsm.render.render2d.font.msdf;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public record Glyph(
        int unicode,
        float advance,
        @Nullable Bounds planeBounds,
        @Nullable Bounds atlasBounds
) {
    public Glyph(JsonObject obj) {
        this(
                obj.get("unicode").getAsInt(),
                obj.get("advance").getAsFloat(),
                obj.has("planeBounds") ? new Bounds(obj.getAsJsonObject("planeBounds")) : null,
                obj.has("atlasBounds") ? new Bounds(obj.getAsJsonObject("atlasBounds")) : null
        );
    }
}
