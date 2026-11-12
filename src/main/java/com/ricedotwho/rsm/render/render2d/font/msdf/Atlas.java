package com.ricedotwho.rsm.render.render2d.font.msdf;

import com.google.gson.JsonObject;

public record Atlas(
            String type,
            int distanceRange,
            int distanceRangeMiddle,
            int size,
            int width,
            int height,
            String yOrigin
) {
    public Atlas(JsonObject object) {
        this(
                object.get("type").getAsString(),
                object.get("distanceRange").getAsInt(),
                object.get("distanceRangeMiddle").getAsInt(),
                object.get("size").getAsInt(),
                object.get("width").getAsInt(),
                object.get("height").getAsInt(),
                object.get("yOrigin").getAsString()
        );
    }
}
