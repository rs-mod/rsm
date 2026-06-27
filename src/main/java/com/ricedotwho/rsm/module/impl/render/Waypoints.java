package com.ricedotwho.rsm.module.impl.render;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.LocationEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineShape;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledShape;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineShape;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@ModuleInfo(aliases = "Waypoints", id = "Waypoints", category = Category.RENDER)
public class Waypoints extends Module {
    private final BooleanSetting placingMode = new BooleanSetting("Placing Mode", false, () -> getPlacingMode().setValue(false), null);
    private final KeybindSetting addWaypoint = new KeybindSetting("Add Waypoint key", new Keybind(InputConstants.UNKNOWN, this::addOrRemoveWaypoint));

    // data
    private final DefaultGroupSetting dataGroup = new DefaultGroupSetting("Placing Data", this);
    private static final ModeSetting renderType = new ModeSetting("Render Typer", "FILLED", List.of("FILLED", "OUTLINE", "FILLED_OUTLINE"));
    private static final ColourSetting colour = new ColourSetting("Place Fill", Colour.GREEN.copy());
    private static final ColourSetting colour2 = new ColourSetting("Place Outline", Colour.GREEN.copy());
    private static final BooleanSetting depth = new BooleanSetting("Place Depth", true);
    private static final NumberSetting lineWidth = new NumberSetting("Place Width", 0.1, 5, 3, 0.1);

    private final SaveSetting<Map<String, List<Waypoint>>> waypoints = new SaveSetting<>(
            "Waypoints",
            "render/waypoints",
            "default.json",
            HashMap::new,
            new TypeToken<@NotNull Map<String, List<Waypoint>>>() {}.getType(),
            true,
            true,
            null
    );

    private List<Waypoint> active = null;

    public Waypoints() {
        this.registerProperty(
                placingMode,
                addWaypoint,
                dataGroup,
                waypoints
        );

        dataGroup.add(renderType, colour, colour2, depth, lineWidth);
    }

    public void setData(WaypointType type, Colour c, Colour c2, boolean d, float w) {
        renderType.setValue(type.name());
        colour.setValue(c);
        colour2.setValue(c2);
        depth.setValue(d);
        lineWidth.setValue(w);
        ChatUtils.chat("Set waypoint data: %s, %s, %s, %s, %s", type.name(), c.getHex(), c2.getHex(), d, w);
    }

    public boolean addOrRemoveWaypoint() {
        if (!placingMode.getValue() || !(mc.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            return false;
        }

        Pos pos = new Pos(blockHitResult.getBlockPos());
        if (com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom() != null) {
            pos = RoomUtils.getRelativePositionFixed(pos, com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom().getUniqueRoom().getMainRoom());
        }
        BlockPos bp = pos.asBlockPos();
        if (removeWaypoint(bp)) {
            ChatUtils.chat("Removed waypoint at %s %s %s", bp.getX(), bp.getY(), bp.getZ());
            return false;
        }
        WaypointType type = EnumUtils.getEnum(WaypointType.class, renderType.getValue(), WaypointType.FILLED);
        Waypoint wp = new Waypoint(pos.asBlockPos(), colour.getValue().copy(), colour2.getValue().copy(), type, depth.getValue(), lineWidth.getValue().floatValue());
        wp.translated = blockHitResult.getBlockPos();
        addWaypoint(wp);
        ChatUtils.chat("Added %s at %s %s %s", type, pos.x(), pos.y(), pos.z());
        return false;
    }

    public void addWaypoint(Waypoint waypoint) {
        getOrCreateList().add(waypoint);
        waypoints.save();
        if (active != null) {
            active.add(waypoint);
        }
    }

    public boolean removeWaypoint(BlockPos pos) {
        List<Waypoint> list = getList();
        if (list == null) return false;
        Optional<Waypoint> a = list.stream().filter(w -> w.pos.equals(pos)).findFirst();
        if (a.isPresent()) {
            list.remove(a.get());
            if (active != null) active.remove(a.get());
            if (list.isEmpty()) waypoints.getValue().remove(Location.getArea().getName());
            waypoints.save();
            return true;
        }
        return false;
    }

    private List<Waypoint> getList() {
        if (Location.getArea().is(Island.Dungeon)) {
            Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
            if (room == null) {
                return waypoints.getValue().get("Catacombs-" + Location.getFloor().getName());
            } else {
                return waypoints.getValue().get("Catacombs-" + room.getUniqueRoom().getName());
            }
        }
        return waypoints.getValue().get(Location.getArea().getName());
    }

    private List<Waypoint> getOrCreateList() {
        if (Location.getArea().is(Island.Dungeon)) {
            Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
            if (room == null) {
                return waypoints.getValue().computeIfAbsent("Catacombs-" + Location.getFloor().getName(), k -> new ArrayList<>());
            } else {
                return waypoints.getValue().computeIfAbsent("Catacombs-" + room.getUniqueRoom().getName(), k -> new ArrayList<>());
            }
        }
        return waypoints.getValue().computeIfAbsent(Location.getArea().getName(), k -> new ArrayList<>());
    }

    @SubscribeEvent
    public void onLocationChanged(LocationEvent.Changed event) {
        if (event.getNewIsland().is(Island.Dungeon)) return;
        List<Waypoint> raw = waypoints.getValue().get(event.getNewIsland().getName());
        if (raw == null) {
            active = new ArrayList<>();
            return;
        }
        active = new ArrayList<>(raw);
    }

    @SubscribeEvent
    public void onDungeonStarted(DungeonEvent.Joined event) {
        List<Waypoint> raw = waypoints.getValue().get("Catacombs-" + event.getFloor().getName());
        if (raw == null) {
            active = new ArrayList<>();
            return;
        }
        active = new ArrayList<>(raw);
    }

    @SubscribeEvent
    public void onScanRoom(DungeonEvent.RoomScanned event) {
        List<Waypoint> temp = waypoints.getValue().get("Catacombs-" + event.getUnique().getName());
        if (temp != null) {
            Room room = event.getUnique().getMainRoom();
            temp.forEach(w -> w.translate(room));
            active.addAll(temp);
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent.Extract event) {
        if (active == null) return;
        active.forEach(Waypoint::render);
    }

    public enum WaypointType {
        FILLED,
        OUTLINE,
        FILLED_OUTLINE
    }

    @RequiredArgsConstructor
    public static class Waypoint {
        private final BlockPos pos;
        public transient BlockPos translated;
        private final Colour colour;
        private final Colour colour2;
        private final WaypointType type;
        private final boolean depth;
        private final float width;

        public void render() {
            if (mc.level == null) return;
            BlockPos p = this.translated == null ? this.pos : this.translated;
            BlockState state = mc.level.getBlockState(p);
            VoxelShape shape = state.getShape(mc.level, p);
            if (shape.isEmpty()) return;
            switch (this.type) {
                case OUTLINE -> Renderer3D.addTask(new OutlineShape(p, shape, this.colour, this.depth, this.width));
                case FILLED -> Renderer3D.addTask(new FilledShape(p, shape, this.colour, this.depth));
                case FILLED_OUTLINE -> Renderer3D.addTask(new FilledOutlineShape(p, shape, this.colour, this.colour2, this.depth, this.width));
            }
        }

        public void translate(Room room) {
            this.translated = RoomUtils.getRealPositionFixed(this.pos, room);
        }
    }
}
