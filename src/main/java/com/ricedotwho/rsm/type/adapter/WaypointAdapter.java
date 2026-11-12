package com.ricedotwho.rsm.type.adapter;

import com.google.gson.*;
import com.ricedotwho.rsm.module.impl.render.Waypoints;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.FileUtils;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Type;

public class WaypointAdapter implements JsonDeserializer<Waypoints.Waypoint>, JsonSerializer<Waypoints.Waypoint> {
    private final Color defaultColor;
    private final OptionalColorAdapter adapter;

    public WaypointAdapter(Color defaultColor) {
        this.defaultColor = defaultColor;
        this.adapter = new OptionalColorAdapter(defaultColor);
    }

    @Override
    public Waypoints.Waypoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        BlockPos pos = context.deserialize(obj.get("pos"), BlockPos.class);
        Waypoints.WaypointType type = Waypoints.WaypointType.valueOf(obj.get("type").getAsString());
        boolean depth = obj.get("depth").getAsBoolean();
        float width = obj.get("width").getAsFloat();
        Color color = obj.has("color") ? adapter.deserialize(obj.get("color"), Color.class, context) : defaultColor.copy();
        Color color2 = obj.has("color2") ? adapter.deserialize(obj.get("color2"), Color.class, context) : defaultColor.copy();

        return new Waypoints.Waypoint(pos, color, color2, type, depth, width);
    }

    @Override
    public JsonElement serialize(Waypoints.Waypoint src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("pos", FileUtils.getGson().toJsonTree(src.pos));
        obj.addProperty("type", src.type.name());
        obj.addProperty("depth", src.depth);
        obj.addProperty("width", src.width);
        obj.addProperty("color", src.color.getHexCode(true));
        obj.addProperty("color2", src.color2.getHexCode(true));

        return obj;
    }
}
