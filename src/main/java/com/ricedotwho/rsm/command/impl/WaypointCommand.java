package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.managers.dungeon.map.Map;
import com.ricedotwho.rsm.managers.dungeon.map.utils.RoomUtils;
import com.ricedotwho.rsm.module.impl.render.Waypoints;
import com.ricedotwho.rsm.type.Pos;
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
                        .executes(_ -> {
                            Waypoints instance = Waypoints.getInstance();
                            instance.getPlacingMode().setValue(!instance.getPlacingMode().getValue());
                            ChatUtils.chat("%s placing mode", instance.getPlacingMode().getValue() ? "Enabled" : "Disabled");
                            return 1;
                        })
                )
                .then(literal("set")
                        .then((literal("filled"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("filled-outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .then(argument("hex2", StringArgumentType.string())
                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), new Color(StringArgumentType.getString(ctx, "hex2").toUpperCase()), false, 3f))
                                                .then(argument("depth", BoolArgumentType.bool())
                                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), new Color(StringArgumentType.getString(ctx, "hex2").toUpperCase()), BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                        .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), new Color(StringArgumentType.getString(ctx, "hex2").toUpperCase()), BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("add")
                        .then((literal("filled"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("filled-outline"))
                                .then(argument("hex", StringArgumentType.string())
                                        .then(argument("hex2", StringArgumentType.string())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), new Color(StringArgumentType.getString(ctx, "hex2").toUpperCase()), false, 3f))
                                                .then(argument("depth", BoolArgumentType.bool())
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), new Color(StringArgumentType.getString(ctx, "hex2").toUpperCase()), BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                        .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, new Color(StringArgumentType.getString(ctx, "hex").toUpperCase()), new Color(StringArgumentType.getString(ctx, "hex2").toUpperCase()), BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private int setData(Waypoints.WaypointType type, Color color, Color color2, boolean depth, float width) {
        Waypoints.getInstance().setData(type, color, color2, depth, width);
        return 1;
    }

    private int addWaypoint(Waypoints.WaypointType type, Color color, Color color2, boolean depth, float width) {
        if (!(mc.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            ChatUtils.chat(ChatFormatting.RED + "Not looking at a block");
            return 0;
        }

        Pos pos = new Pos(blockHitResult.getBlockPos());
        if (Map.getCurrentRoom() != null) {
            pos = RoomUtils.getRelativePositionFixed(pos, Map.getCurrentRoom().getUniqueRoom().getMainRoom());
        }
        BlockPos bp = pos.asBlockPos();
        Waypoints.Waypoint wp = new Waypoints.Waypoint(bp, color, color2, type, depth, width);
        wp.translated = bp;
        Waypoints.getInstance().addWaypoint(wp);
        return 1;
    }
}
