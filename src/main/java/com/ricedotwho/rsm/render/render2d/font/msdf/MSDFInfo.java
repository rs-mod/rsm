package com.ricedotwho.rsm.render.render2d.font.msdf;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public record MSDFInfo(Atlas atlas, Metrics metrics, Int2ObjectOpenHashMap<Glyph> glyphs) {
    public MSDFInfo(JsonObject obj) {
        var a = new Atlas(obj.get("atlas").getAsJsonObject());
        var m = new Metrics(obj.get("metrics").getAsJsonObject());

        var map = new Int2ObjectOpenHashMap<Glyph>();

        obj.getAsJsonArray("glyphs").forEach(it -> {
            if (!it.isJsonObject()) return;
            var g = new Glyph(it.getAsJsonObject());
            map.put(g.unicode(), g);
        });

        this(a, m, map);
    }
}
