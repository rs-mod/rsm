package com.ricedotwho.rsm.type.adapter;

import com.google.gson.*;
import com.ricedotwho.rsm.type.Color;
import lombok.val;

import java.lang.reflect.Type;

public class ColorAdapter implements JsonDeserializer<Color>, JsonSerializer<Color> {
    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("hex")) return null;
        val color = Color.WHITE.clone();

        val potentialARGB = Color.parseHex(obj.get("hex").getAsString(), true);
        potentialARGB.ifPresent(color::setToColor);
        return color;
    }

    @Override
    public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        // flames yo
        if (src == null) return obj;
        obj.addProperty("hex", src.getHexCode(true));
        return obj;
    }
}
