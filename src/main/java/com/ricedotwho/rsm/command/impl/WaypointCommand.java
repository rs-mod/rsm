package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.render.Waypoints;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@CommandInfo(name = "wp", description = "Set waypoint data")
public class WaypointCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("t")
                        .executes(ctx -> {
                            Waypoints instance = RSM.getModule(Waypoints.class);
                            instance.getPlacingMode().setValue(!instance.getPlacingMode().getValue());
                            ChatUtils.chat("%s placing mode", instance.getPlacingMode().getValue() ? "Enabled" : "Disabled");
                            return 1;
                        })
                )
                .then(literal("set")
                        .then((literal("filled"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("filled-outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .then(argument("hex2", StringArgumentType.string())
                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), new Colour(StringArgumentType.getString(ctx, "hex2")), false, 3f))
                                                .then(argument("depth", BoolArgumentType.bool())
                                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), new Colour(StringArgumentType.getString(ctx, "hex2")), BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                        .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), new Colour(StringArgumentType.getString(ctx, "hex2")), BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("add")
                        .then((literal("filled"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), Colour.CYAN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("filled-outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .then(argument("hex2", StringArgumentType.string())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), new Colour(StringArgumentType.getString(ctx, "hex2")), false, 3f))
                                                .then(argument("depth", BoolArgumentType.bool())
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), new Colour(StringArgumentType.getString(ctx, "hex2")), BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                        .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, new Colour(StringArgumentType.getString(ctx, "hex")), new Colour(StringArgumentType.getString(ctx, "hex2")), BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private int setData(Waypoints.WaypointType type, Colour colour, Colour colour2, boolean depth, float width) {
        RSM.getModule(Waypoints.class).setData(type, colour, colour2, depth, width);
        ChatUtils.chat("Set waypoint data");
        return 1;
    }

    private int addWaypoint(Waypoints.WaypointType type, Colour colour, Colour colour2, boolean depth, float width) {
        if (!(mc.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            ChatUtils.chat(ChatFormatting.RED + "Not looking at a block");
            return 0;
        }

        Pos pos = new Pos(blockHitResult.getBlockPos());
        if (Map.getCurrentRoom() != null) {
            pos = RoomUtils.getRelativePositionFixed(pos, Map.getCurrentRoom().getUniqueRoom().getMainRoom());
        }
        BlockPos bp = pos.asBlockPos();
        Waypoints.Waypoint wp = new Waypoints.Waypoint(bp, colour, colour2, type, depth, width);
        wp.translated = bp;
        RSM.getModule(Waypoints.class).addWaypoint(wp);
        return 1;
    }
}
