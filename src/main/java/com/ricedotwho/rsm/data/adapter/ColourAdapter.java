package com.ricedotwho.rsm.data.adapter;

import com.google.gson.*;
import com.ricedotwho.rsm.data.Colour;

import java.lang.reflect.Type;

public class ColourAdapter  implements JsonDeserializer<Colour>, JsonSerializer<Colour> {
    @Override
    public Colour deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("hue")) return null;
        short h = obj.get("hue").getAsShort();
        short s = obj.get("saturation").getAsShort();
        short b = obj.get("brightness").getAsShort();
        short a = obj.get("alpha").getAsShort();
        int dataBit = obj.get("dataBit").getAsInt();
        return new Colour(h, s, b, a, dataBit);
    }

    @Override
    public JsonElement serialize(Colour src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        // flames yo
        if (src == null) return obj;
        obj.addProperty("hue", src.getHue());
        obj.addProperty("saturation", src.getSaturation());
        obj.addProperty("brightness", src.getBrightness());
        obj.addProperty("alpha", src.getAlpha());
        obj.addProperty("dataBit", src.getDataBitRaw());
        return obj;
    }
}
