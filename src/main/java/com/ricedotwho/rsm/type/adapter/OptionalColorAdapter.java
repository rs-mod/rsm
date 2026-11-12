package com.ricedotwho.rsm.type.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;

import java.lang.reflect.Type;

@AllArgsConstructor
public class OptionalColorAdapter implements JsonDeserializer<Color> {
    private final Color def;

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            var colour = Color.WHITE.clone();
            var potentialARGB = Color.parseHex(json.getAsString(), true);
            potentialARGB.ifPresent(colour::setToColor);
            return colour;
        }
        return this.def.copy();
    }
}
