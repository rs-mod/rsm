package com.ricedotwho.rsm.type.adapter;

import com.google.gson.*;
import com.ricedotwho.rsm.module.impl.render.Waypoints;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Type;

public class WaypointAdapter implements JsonDeserializer<Waypoints.Waypoint> {

    private final Color defaultColor;

    public WaypointAdapter(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    @Override
    public Waypoints.Waypoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        BlockPos pos = context.deserialize(obj.get("pos"), BlockPos.class);
        Waypoints.WaypointType type = Waypoints.WaypointType.valueOf(obj.get("type").getAsString());
        boolean depth = obj.get("depth").getAsBoolean();
        float width = obj.get("width").getAsFloat();
        Color color = obj.has("color") ? context.deserialize(obj.get("color"), Color.class) : defaultColor.copy();
        Color color2 = obj.has("color2") ? context.deserialize(obj.get("color2"), Color.class) : defaultColor.copy();

        return new Waypoints.Waypoint(pos, color, color2, type, depth, width);
    }
}
