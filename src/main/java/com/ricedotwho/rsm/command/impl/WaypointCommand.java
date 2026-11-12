package com.ricedotwho.rsm.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.module.impl.render.Waypoints;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandInfo(name = "wp", description = "Set waypoint data")
public class WaypointCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("t")
                        .executes(_ -> {
                            Waypoints instance = Waypoints.getInstance();
                            instance.getPlacingMode().setValue(!instance.getPlacingMode().getValue());
                            ChatUtils.chat("{} placing mode", instance.getPlacingMode().getValue() ? "Enabled" : "Disabled");
                            return 1;
                        })
                )
                .then(literal("set")
                        .then((literal("filled"))
                                .then(argument("hex", HexColorArgument.hexColor())
                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("outline"))
                                .then(argument("hex", HexColorArgument.hexColor())
                                        .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> setData(Waypoints.WaypointType.OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("filled-outline"))
                                .then(argument("hex", HexColorArgument.hexColor())
                                        .then(argument("hex2", HexColorArgument.hexColor())
                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.fromARGB(ctx.getArgument("hex2", Integer.class)), false, 3f))
                                                .then(argument("depth", BoolArgumentType.bool())
                                                        .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.fromARGB(ctx.getArgument("hex2", Integer.class)), BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                        .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                                .executes(ctx -> setData(Waypoints.WaypointType.FILLED_OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.fromARGB(ctx.getArgument("hex2", Integer.class)), BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("add")
                        .then((literal("filled"))
                                .then(argument("hex", HexColorArgument.hexColor())
                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("outline"))
                                .then(argument("hex", HexColorArgument.hexColor())
                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, false, 3f))
                                        .then(argument("depth", BoolArgumentType.bool())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.GREEN, BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                )
                                        )
                                )
                        )
                        .then((literal("filled-outline"))
                                .then(argument("hex", HexColorArgument.hexColor())
                                        .then(argument("hex2", HexColorArgument.hexColor())
                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.fromARGB(ctx.getArgument("hex2", Integer.class)), false, 3f))
                                                .then(argument("depth", BoolArgumentType.bool())
                                                        .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.fromARGB(ctx.getArgument("hex2", Integer.class)), BoolArgumentType.getBool(ctx, "depth"), 3f))
                                                        .then(argument("width", FloatArgumentType.floatArg(0.01f, 10f))
                                                                .executes(ctx -> addWaypoint(Waypoints.WaypointType.FILLED_OUTLINE, Color.fromARGB(ctx.getArgument("hex", Integer.class)), Color.fromARGB(ctx.getArgument("hex2", Integer.class)), BoolArgumentType.getBool(ctx, "depth"), FloatArgumentType.getFloat(ctx, "width")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("convert")
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    convertLCM(StringArgumentType.getString(ctx, "name"));
                                    return 1;
                                })
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

        Vec3 vec3 = new Vec3(blockHitResult.getBlockPos());
        if (Dungeon.current() != null) {
            vec3 = Dungeon.current().getRelativePositionFixed(vec3);
        }
        BlockPos bp = vec3.toBlockPos();
        Waypoints.Waypoint wp = new Waypoints.Waypoint(bp, color, color2, type, depth, width);
        wp.translated = bp;
        Waypoints.getInstance().addWaypoint(wp);
        return 1;
    }

    private void convertLCM(String saveTo) {
        try {
            JsonObject data = JsonParser.parseString(mc.keyboardHandler.getClipboard()).getAsJsonObject();
            Map<String, List<Waypoints.Waypoint>> temp = new HashMap<>();

            data.entrySet().forEach(it -> {
                var list = it.getValue().getAsJsonArray().asList().stream().map(e -> convert(e.getAsJsonObject())).toList();
                temp.put(it.getKey(), list);
            });

            var file = FileUtils.getSaveFileInCategory("render/waypoints", "$saveTo.json");
            FileUtils.checkDir(file, new HashMap<>());

            try {
                Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
                Waypoints.getInstance().getWaypoints().getGson().toJson(temp, writer);
                writer.close();

                var totalSize = temp.values().stream().mapToLong(List::size).sum();

                ChatUtils.chat("${ChatFormatting.GREEN}Converted $totalSize waypoints to $saveTo");

            } catch (IOException e) {
                ChatUtils.chat("Failed to write lcm waypoints!");
                RSM.getLogger().error("Error saving lcm converted waypoints {}", temp, e);
            }

        } catch (Exception e) {
            ChatUtils.chat("${ChatFormatting.RED}Failed to convert waypoints! (${e.getMessage()})");
            RSM.getLogger().error("Error while converting lcm waypoints", e);
        }
    }

    private Waypoints.Waypoint convert(JsonObject obj) {
        var p = obj.get("pos").getAsString().split(",");
        var pos = new BlockPos(p[0].toInt() - 15, p[1].toInt(), p[2].toInt() - 15);
        var depth = !obj.get("throughWalls").getAsBoolean();
        return new Waypoints.Waypoint(pos,
                Waypoints.getInstance().getColor().getValue(),
                Waypoints.getInstance().getColor2().getValue(),
                Waypoints.WaypointType.FILLED,
                depth,
                Waypoints.getInstance().getLineWidth().getValue()
        );
    }
}
