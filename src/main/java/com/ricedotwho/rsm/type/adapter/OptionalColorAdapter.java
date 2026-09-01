package com.ricedotwho.rsm.type.adapter;

import com.google.gson.*;
import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;
import lombok.val;

import java.lang.reflect.Type;

@AllArgsConstructor
public class OptionalColorAdapter implements JsonDeserializer<Color> {
    private final Color def;

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("hex")) return this.def.copy();
        val color = Color.WHITE.clone();

        val potentialARGB = Color.parseHex(obj.get("hex").getAsString(), true);
        potentialARGB.ifPresent(color::setToColor);
        return color;
    }
}
