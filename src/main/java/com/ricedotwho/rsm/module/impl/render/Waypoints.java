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
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineShape;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledShape;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineShape;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Waypoints", id = "Waypoints", category = Category.RENDER)
public class Waypoints extends Module {
    private final BooleanSetting placingMode = new BooleanSetting("Placing Mode", false, () -> getPlacingMode().setValue(false), null);
    private final KeybindSetting addWaypoint = new KeybindSetting("Add Waypoint key", new Keybind(InputConstants.UNKNOWN, this::addOrRemoveWaypoint));
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

    private final Data data = new Data(WaypointType.FILLED, Colour.GREEN, Colour.GREEN, true, 3f);

    private List<Waypoint> active = null;

    public Waypoints() {
        this.registerProperty(
                placingMode,
                addWaypoint,
                waypoints
        );
    }

    public void setData(WaypointType type, Colour colour, Colour colour2, boolean depth, float width) {
        data.colour = colour;
        data.colour2 = colour2;
        data.depth = depth;
        data.type = type;
        data.width = width;
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
        Waypoint wp =  new Waypoint(pos.asBlockPos(), data.colour, data.colour2, data.type, data.depth, data.width);
        wp.translated = bp;
        addWaypoint(wp);
        ChatUtils.chat("Added %s at %s %s %s", data.type, pos.x(), pos.y(), pos.z());
        return false;
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.getValue().computeIfAbsent(Location.getArea().getName(), k -> new ArrayList<>()).add(waypoint);
        waypoints.save();
        update(Location.getArea());
    }

    public boolean removeWaypoint(BlockPos pos) {
        List<Waypoint> list = waypoints.getValue().get(Location.getArea().getName());
        if (list == null) return false;
        boolean a = list.removeIf(w -> w.pos.equals(pos));
        if (list.isEmpty()) {
            waypoints.getValue().remove(Location.getArea().getName());
        }
        waypoints.save();
        update(Location.getArea());
        return a;
    }

    private void update(Island island) {
        active = waypoints.getValue().get(island.getName());
    }

    @SubscribeEvent
    public void onLocationChanged(LocationEvent.Changed event) {
        active = waypoints.getValue().get(event.getNewIsland().getName());
    }

    @SubscribeEvent
    public void onDungeonStarted(DungeonEvent.Start event) {
        active = waypoints.getValue().get("Catacombs-" + event.getFloor().getName());
    }

    @SubscribeEvent
    public void onRoomChanged(DungeonEvent.ChangeRoom event) {
        active = waypoints.getValue().get("Catacombs-" + event.getRoom().getUniqueRoom().getName());
        if (active != null) {
            Room room = event.getUnique().getMainRoom();
            active.forEach(w -> w.translate(room));
        }
    }

    @SubscribeEvent
    public void onEnterBoss(DungeonEvent.EnterBoss event) {
        active = waypoints.getValue().get("Catacombs-" + event.getFloor().getName());
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

    @AllArgsConstructor
    public static class Data {
        public WaypointType type;
        public Colour colour;
        public Colour colour2;
        public boolean depth;
        public float width;
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
