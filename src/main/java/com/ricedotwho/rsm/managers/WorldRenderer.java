package com.ricedotwho.rsm.managers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.render.render3d.type.*;
import com.ricedotwho.rsm.type.Color;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

import java.util.*;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Register
@SuppressWarnings("unused")
@UtilityClass
public class WorldRenderer {
    private final List<Beacon> beacons = new ArrayList<>();
    private final List<Text> texts = new ArrayList<>();

    private final Map<Class<? extends RenderTask>, TaskList<? extends RenderTask>> lineMap = new HashMap<>();
    private final Map<Class<? extends RenderTask>, TaskList<? extends RenderTask>> filledMap = new HashMap<>();

    @Init
    private void init() {
        // Filled
        registerFilled(FilledBox.class);
        registerFilled(FilledOutlineBox.class);
        registerFilled(FilledShape.class);
        registerFilled(FilledOutlineShape.class);

        // Lines
        registerLine(Circle.class);
        registerLine(FilledOutlineBox.class);
        registerLine(Line.class);
        registerLine(OutlineBox.class);
        registerLine(LineList.class);
        registerLine(Rectangle.class);
        registerLine(OutlineShape.class);
        registerLine(FilledOutlineShape.class);
        registerLine(Ring.class);
    }


    @SuppressWarnings("unchecked")
    private <T extends RenderTask> TaskList<T> getLineList(Class<T> type) {
        return (TaskList<T>) lineMap.get(type);
    }

    @SuppressWarnings("unchecked")
    private <T extends RenderTask> TaskList<T> getFilledList(Class<T> type) {
        return (TaskList<T>) filledMap.get(type);
    }

    private <T extends RenderTask> void registerLine(Class<T> type, TaskList<T> list) {
        lineMap.put(type, list);
    }

    private <T extends RenderTask> void registerLine(Class<T> type) {
        registerLine(type, new TaskList<>());
    }

    private <T extends RenderTask> void registerFilled(Class<T> type, TaskList<T> list) {
        filledMap.put(type, list);
    }

    private <T extends RenderTask> void registerFilled(Class<T> type) {
        registerFilled(type, new TaskList<>());
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent.Last event) {
        try {
            PoseStack stack = event.getContext().poseStack();
            Vec3 camera = mc.gameRenderer.mainCamera().position();
            LevelRenderContext ctx = event.getContext();
            SubmitNodeCollector collector = ctx.submitNodeCollector();

            renderGizmos();
            renderBatchedBeaconBeams(stack, camera, collector);
        } finally { //just in case;;;
            clear();
        }
    }

    private void clear() {
        lineMap.forEach((_, e) -> e.clear());
        filledMap.forEach((_, e) -> e.clear());
        texts.clear();
        beacons.clear();
    }

    // MC 26.3 removed MultiBufferSource/raw vertex submission for custom geometry.
    // RSM rendering is emitted through the new net.minecraft.gizmos.* system, which is
    // collected on the render thread by LevelRenderer during the level render pass.
    private void renderGizmos() {
        java.util.Set<RenderTask> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (TaskList<? extends RenderTask> taskSet : lineMap.values()) {
            for (RenderTask task : taskSet.depth) if (seen.add(task)) renderTask(task);
            for (RenderTask task : taskSet.noDepth) if (seen.add(task)) renderTask(task);
        }
        for (TaskList<? extends RenderTask> taskList : filledMap.values()) {
            for (RenderTask task : taskList.depth) if (seen.add(task)) renderTask(task);
            for (RenderTask task : taskList.noDepth) if (seen.add(task)) renderTask(task);
        }
        // TODO(26.3): Text.rotate/dropShadow cannot be expressed via net.minecraft.gizmos.TextGizmo;
        //              scale semantics differ from the old Font.drawInBatch path.
        for (Text task : texts) {
            GizmoProperties props = Gizmos.billboardText(task.getContent(), task.getPos(),
                    TextGizmo.Style.forColorAndCentered(task.getColor()).withScale(task.getScale() * 0.025f));
            withDepth(task.isDepth(), props);
        }
    }

    private void renderTask(RenderTask task) {
        if (task instanceof FilledBox box) {
            withDepth(box.isDepth(), Gizmos.cuboid(box.getAabb(), GizmoStyle.fill(box.getColor())));
        } else if (task instanceof FilledOutlineBox box) {
            withDepth(box.isDepth(), Gizmos.cuboid(box.getAabb(), GizmoStyle.strokeAndFill(box.getLine(), box.getWidth(), box.getFill())));
        } else if (task instanceof OutlineBox box) {
            withDepth(box.isDepth(), Gizmos.cuboid(box.getAabb(), GizmoStyle.stroke(box.getColor(), box.getWidth())));
        } else if (task instanceof Circle circle) {
            // TODO(26.3): Gizmos.circle cannot honour the requested slice count.
            withDepth(circle.isDepth(), Gizmos.circle(circle.getPos(), circle.getRadius(), GizmoStyle.stroke(circle.getColor(), circle.getWidth())));
        } else if (task instanceof Ring ring) {
            // TODO(26.3): Ring renders as a solid filled disc (triangle fan); the old layered annulus had a hollow centre.
            if (distSq(ring.getPos()) > 64.0 * 64.0) return;
            withDepth(ring.isDepth(), Gizmos.circle(ring.getPos(), ring.getRadius(), GizmoStyle.fill(ring.getColor())));
        } else if (task instanceof Line line) {
            // TODO(26.3): Gizmos.line is single-colour; the start->end gradient is dropped in favour of the start colour.
            withDepth(line.isDepth(), Gizmos.line(line.getFrom(), line.getTo(), line.getStart(), line.getWidth()));
        } else if (task instanceof LineList list) {
            List<Vec3> positions = list.getPositions();
            int size = positions.size() - 1;
            for (int i = 0; i < size; i++) {
                float t0 = (float) i / size;
                int c = Color.lerpARGB(list.getStart(), list.getEnd(), t0);
                withDepth(list.isDepth(), Gizmos.line(positions.get(i), positions.get(i + 1), c, list.getWidth()));
            }
        } else if (task instanceof Rectangle rect) {
            // TODO(26.3): approximates the flat horizontal rect with Gizmos.rect at aabb.minY.
            AABB aabb = rect.getAabb();
            float y = (float) aabb.minY;
            GizmoStyle style = GizmoStyle.stroke(rect.getColor(), rect.getLineWidth());
            withDepth(rect.isDepth(), Gizmos.rect(
                    new Vec3(aabb.minX, y, aabb.minZ),
                    new Vec3(aabb.maxX, y, aabb.minZ),
                    new Vec3(aabb.maxX, y, aabb.maxZ),
                    new Vec3(aabb.minX, y, aabb.maxZ),
                    style));
        } else if (task instanceof OutlineShape outline) {
            GizmoStyle style = GizmoStyle.stroke(outline.getColor(), outline.getWidth());
            outline.getShape().forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    withDepth(outline.isDepth(), Gizmos.cuboid(offset(outline.getPos(), x1, y1, z1, x2, y2, z2), style)));
        } else if (task instanceof FilledShape filled) {
            GizmoStyle style = GizmoStyle.fill(filled.getColor());
            filled.getShape().forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    withDepth(filled.isDepth(), Gizmos.cuboid(offset(filled.getPos(), x1, y1, z1, x2, y2, z2), style)));
        } else if (task instanceof FilledOutlineShape outline) {
            GizmoStyle style = GizmoStyle.strokeAndFill(outline.getLine(), outline.getWidth(), outline.getFill());
            outline.getShape().forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    withDepth(outline.isDepth(), Gizmos.cuboid(offset(outline.getPos(), x1, y1, z1, x2, y2, z2), style)));
        }
    }

    private static AABB offset(BlockPos pos, double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AABB(pos.getX() + x1, pos.getY() + y1, pos.getZ() + z1, pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2);
    }

    private void withDepth(boolean depth, GizmoProperties props) {
        if (!depth) props.setAlwaysOnTop();
    }

    private static double distSq(Vec3 pos) {
        Entity camera = mc.getCameraEntity();
        return camera == null ? Double.MAX_VALUE : camera.distanceToSqr(pos);
    }

    private void renderBatchedBeaconBeams(PoseStack stack, Vec3 camera, SubmitNodeCollector collector) {
        for (Beacon task : beacons) {
            task.renderBeacon(stack, camera, collector);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends RenderTask> void addTask(T task) {
        TaskList<T> set;
        switch (task.getType()) {
            case LINE -> set = getLineList((Class<T>) task.getClass());
            case FILLED -> set = getFilledList((Class<T>) task.getClass());
            case FILLED_OUTLINE -> {
                getLineList((Class<T>) task.getClass()).add(task);
                getFilledList((Class<T>) task.getClass()).add(task);
                return;
            }
            case BEACON -> {
                beacons.add((Beacon) task);
                return;
            }
            case TEXT -> {
                texts.add((Text) task);
                return;
            }
            default -> {
                return;
            }
        }
        set.add(task);
    }

    private final float DEFAULT_LINE_WIDTH = 3f;
    private final int DEFAULT_RING_LAYERS = 16;
    private final int DEFAULT_SLICE_COUNT = 48;
    private final float DEFAULT_TEXT_SCALE = 1f;

    private AABB cubeAround(Vec3 pos, double scale) {
        double half = scale / 2;
        return new AABB(pos.x - half, pos.y - half, pos.z - half, pos.x + half, pos.y + half, pos.z + half);
    }

    public void beacon(Vec3 pos, int color) {
        addTask(new Beacon(pos, color));
    }

    public void beacon(Vec3 pos, Color color) {
        addTask(new Beacon(pos, color.getARGB()));
    }

    public void circle(Vec3 pos, boolean depth, float radius, int color, int slices) {
        addTask(new Circle(pos, depth, radius, color, slices, DEFAULT_LINE_WIDTH));
    }

    public void circle(Vec3 pos, boolean depth, float radius, int color, int slices, float width) {
        addTask(new Circle(pos, depth, radius, color, slices, width));
    }

    public void circle(Vec3 pos, boolean depth, float radius, Color color, int slices) {
        addTask(new Circle(pos, depth, radius, color.getARGB(), slices, DEFAULT_LINE_WIDTH));
    }

    public void circle(Vec3 pos, boolean depth, float radius, Color color, int slices, float width) {
        addTask(new Circle(pos, depth, radius, color.getARGB(), slices, width));
    }

    public void filledBox(AABB aabb, int color) {
        addTask(new FilledBox(aabb, color, true));
    }

    public void filledBox(AABB aabb, int color, boolean depth) {
        addTask(new FilledBox(aabb, color, depth));
    }

    public void filledBox(BlockPos pos, int color) {
        filledBox(new AABB(pos), color, true);
    }

    public void filledBox(BlockPos pos, int color, boolean depth) {
        filledBox(new AABB(pos), color, depth);
    }

    public void filledBox(AABB aabb, Color color) {
        filledBox(aabb, color.getARGB(), true);
    }

    public void filledBox(AABB aabb, Color color, boolean depth) {
        filledBox(aabb, color.getARGB(), depth);
    }

    public void filledBox(BlockPos pos, Color color) {
        filledBox(new AABB(pos), color.getARGB(), true);
    }

    public void filledBox(BlockPos pos, Color color, boolean depth) {
        filledBox(new AABB(pos), color.getARGB(), depth);
    }

    public void filledOutlineBox(AABB aabb, int fill, int line) {
        addTask(new FilledOutlineBox(aabb, fill, line, true, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineBox(AABB aabb, int fill, int line, boolean depth) {
        addTask(new FilledOutlineBox(aabb, fill, line, depth, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineBox(AABB aabb, int fill, int line, boolean depth, float width) {
        addTask(new FilledOutlineBox(aabb, fill, line, depth, width));
    }

    public void filledOutlineBox(AABB aabb, Color fill, Color line) {
        filledOutlineBox(aabb, fill.getARGB(), line.getARGB(), true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, Color fill, Color line, boolean depth) {
        filledOutlineBox(aabb, fill.getARGB(), line.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, Color fill, Color line, boolean depth, float width) {
        filledOutlineBox(aabb, fill.getARGB(), line.getARGB(), depth, width);
    }

    public void filledOutlineBox(BlockPos pos, int fill, int line) {
        filledOutlineBox(new AABB(pos), fill, line, true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, int fill, int line, boolean depth) {
        filledOutlineBox(new AABB(pos), fill, line, depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, int fill, int line, boolean depth, float width) {
        filledOutlineBox(new AABB(pos), fill, line, depth, width);
    }

    public void filledOutlineBox(BlockPos pos, Color fill, Color line) {
        filledOutlineBox(new AABB(pos), fill.getARGB(), line.getARGB(), true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, Color fill, Color line, boolean depth) {
        filledOutlineBox(new AABB(pos), fill.getARGB(), line.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, Color fill, Color line, boolean depth, float width) {
        filledOutlineBox(new AABB(pos), fill.getARGB(), line.getARGB(), depth, width);
    }

    public void filledOutlineBox(AABB aabb, int color) {
        filledOutlineBox(aabb, color, color, true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, int color, boolean depth) {
        filledOutlineBox(aabb, color, color, depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, int color, boolean depth, float width) {
        filledOutlineBox(aabb, color, color, depth, width);
    }

    public void filledOutlineBox(AABB aabb, Color color) {
        filledOutlineBox(aabb, color.getARGB(), color.getARGB(), true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, Color color, boolean depth) {
        filledOutlineBox(aabb, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, Color color, boolean depth, float width) {
        filledOutlineBox(aabb, color.getARGB(), color.getARGB(), depth, width);
    }

    public void filledOutlineBox(BlockPos pos, int color) {
        filledOutlineBox(new AABB(pos), color, color, true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, int color, boolean depth) {
        filledOutlineBox(new AABB(pos), color, color, depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, int color, boolean depth, float width) {
        filledOutlineBox(new AABB(pos), color, color, depth, width);
    }

    public void filledOutlineBox(BlockPos pos, Color color) {
        filledOutlineBox(new AABB(pos), color.getARGB(), color.getARGB(), true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, Color color, boolean depth) {
        filledOutlineBox(new AABB(pos), color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, Color color, boolean depth, float width) {
        filledOutlineBox(new AABB(pos), color.getARGB(), color.getARGB(), depth, width);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int fill, int line) {
        addTask(new FilledOutlineShape(pos, shape, fill, line, true, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int fill, int line, boolean depth) {
        addTask(new FilledOutlineShape(pos, shape, fill, line, depth, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int fill, int line, boolean depth, float width) {
        addTask(new FilledOutlineShape(pos, shape, fill, line, depth, width));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line) {
        addTask(new FilledOutlineShape(pos, shape, fill.getARGB(), line.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line, boolean depth) {
        addTask(new FilledOutlineShape(pos, shape, fill.getARGB(), line.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line, boolean depth, float width) {
        addTask(new FilledOutlineShape(pos, shape, fill.getARGB(), line.getARGB(), depth, width));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int color) {
        filledOutlineShape(pos, shape, color, color, true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth) {
        filledOutlineShape(pos, shape, color, color, depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth, float width) {
        filledOutlineShape(pos, shape, color, color, depth, width);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color color) {
        filledOutlineShape(pos, shape, color.getARGB(), color.getARGB(), true, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth) {
        filledOutlineShape(pos, shape, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth, float width) {
        filledOutlineShape(pos, shape, color.getARGB(), color.getARGB(), depth, width);
    }

    public void filledShape(BlockPos pos, VoxelShape shape, int color, boolean depth) {
        addTask(new FilledShape(pos, shape, color, depth));
    }

    public void filledShape(BlockPos pos, VoxelShape shape, Color color, boolean depth) {
        addTask(new FilledShape(pos, shape, color.getARGB(), depth));
    }

    public void filledShape(BlockPos pos, VoxelShape shape, int color) {
        addTask(new FilledShape(pos, shape, color, true));
    }

    public void filledShape(BlockPos pos, VoxelShape shape, Color color) {
        addTask(new FilledShape(pos, shape, color.getARGB(), true));
    }

    public void line(Vec3 from, Vec3 to, int start, int end) {
        addTask(new Line(from, to, start, end, true, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, int start, int end, boolean depth) {
        addTask(new Line(from, to, start, end, depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, int start, int end, boolean depth, float width) {
        addTask(new Line(from, to, start, end, depth, width));
    }

    public void line(Vec3 from, Vec3 to, Color start, Color end) {
        addTask(new Line(from, to, start.getARGB(), end.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, Color start, Color end, boolean depth) {
        addTask(new Line(from, to, start.getARGB(), end.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, Color start, Color end, boolean depth, float width) {
        addTask(new Line(from, to, start.getARGB(), end.getARGB(), depth, width));
    }

    public void line(Vec3 from, Vec3 to, int color) {
        addTask(new Line(from, to, color, color, true, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, int color, boolean depth) {
        addTask(new Line(from, to, color, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, int color, boolean depth, float width) {
        addTask(new Line(from, to, color, color, depth, width));
    }

    public void line(Vec3 from, Vec3 to, Color color) {
        addTask(new Line(from, to, color.getARGB(), color.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, Color color, boolean depth) {
        addTask(new Line(from, to, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, Color color, boolean depth, float width) {
        addTask(new Line(from, to, color.getARGB(), color.getARGB(), depth, width));
    }

    public void lineList(List<Vec3> positions, int start, int end) {
        addTask(new LineList(positions, start, end, true, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, int start, int end, boolean depth) {
        addTask(new LineList(positions, start, end, depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, int start, int end, boolean depth, float width) {
        addTask(new LineList(positions, start, end, depth, width));
    }

    public void lineList(List<Vec3> positions, Color start, Color end) {
        addTask(new LineList(positions, start.getARGB(), end.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, Color start, Color end, boolean depth) {
        addTask(new LineList(positions, start.getARGB(), end.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, Color start, Color end, boolean depth, float width) {
        addTask(new LineList(positions, start.getARGB(), end.getARGB(), depth, width));
    }

    public void lineList(List<Vec3> positions, int color) {
        addTask(new LineList(positions, color, color, true, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, int color, boolean depth) {
        addTask(new LineList(positions, color, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, int color, boolean depth, float width) {
        addTask(new LineList(positions, color, color, depth, width));
    }

    public void lineList(List<Vec3> positions, Color color) {
        addTask(new LineList(positions, color.getARGB(), color.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, Color color, boolean depth) {
        addTask(new LineList(positions, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, Color color, boolean depth, float width) {
        addTask(new LineList(positions, color.getARGB(), color.getARGB(), depth, width));
    }

    public void outlineBox(AABB aabb, int color) {
        addTask(new OutlineBox(aabb, color, true, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(AABB aabb, int color, boolean depth) {
        addTask(new OutlineBox(aabb, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(AABB aabb, int color, boolean depth, float width) {
        addTask(new OutlineBox(aabb, color, depth, width));
    }

    public void outlineBox(AABB aabb, Color color) {
        addTask(new OutlineBox(aabb, color.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(AABB aabb, Color color, boolean depth) {
        addTask(new OutlineBox(aabb, color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(AABB aabb, Color color, boolean depth, float width) {
        addTask(new OutlineBox(aabb, color.getARGB(), depth, width));
    }

    public void outlineBox(BlockPos pos, int color) {
        addTask(new OutlineBox(new AABB(pos), color, true, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(BlockPos pos, int color, boolean depth) {
        addTask(new OutlineBox(new AABB(pos), color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(BlockPos pos, Color color) {
        addTask(new OutlineBox(new AABB(pos), color.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(BlockPos pos, Color color, boolean depth) {
        addTask(new OutlineBox(new AABB(pos), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(Vec3 pos, double scale, int color) {
        addTask(new OutlineBox(cubeAround(pos, scale), color, true, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(Vec3 pos, double scale, int color, boolean depth) {
        addTask(new OutlineBox(cubeAround(pos, scale), color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(Vec3 pos, double scale, Color color) {
        addTask(new OutlineBox(cubeAround(pos, scale), color.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(Vec3 pos, double scale, Color color, boolean depth) {
        addTask(new OutlineBox(cubeAround(pos, scale), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, int color) {
        addTask(new OutlineShape(pos, shape, color, true, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth) {
        addTask(new OutlineShape(pos, shape, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth, float width) {
        addTask(new OutlineShape(pos, shape, color, depth, width));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, Color color) {
        addTask(new OutlineShape(pos, shape, color.getARGB(), true, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth) {
        addTask(new OutlineShape(pos, shape, color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth, float width) {
        addTask(new OutlineShape(pos, shape, color.getARGB(), depth, width));
    }

    public void rectangle(AABB aabb, int color) {
        addTask(new Rectangle(aabb, color, DEFAULT_LINE_WIDTH, true));
    }

    public void rectangle(AABB aabb, int color, boolean depth) {
        addTask(new Rectangle(aabb, color, DEFAULT_LINE_WIDTH, depth));
    }

    public void rectangle(AABB aabb, int color, float lineWidth) {
        addTask(new Rectangle(aabb, color, lineWidth, true));
    }

    public void rectangle(AABB aabb, int color, float lineWidth, boolean depth) {
        addTask(new Rectangle(aabb, color, lineWidth, depth));
    }

    public void rectangle(AABB aabb, Color color) {
        addTask(new Rectangle(aabb, color.getARGB(), DEFAULT_LINE_WIDTH, true));
    }

    public void rectangle(AABB aabb, Color color, boolean depth) {
        addTask(new Rectangle(aabb, color.getARGB(), DEFAULT_LINE_WIDTH, depth));
    }

    public void rectangle(AABB aabb, Color color, float lineWidth) {
        addTask(new Rectangle(aabb, color.getARGB(), lineWidth, true));
    }

    public void rectangle(AABB aabb, Color color, float lineWidth, boolean depth) {
        addTask(new Rectangle(aabb, color.getARGB(), lineWidth, depth));
    }

    public void ring(Vec3 pos, boolean depth, float radius, int color) {
        addTask(new Ring(pos, depth, radius, color, DEFAULT_SLICE_COUNT, DEFAULT_RING_LAYERS));
    }

    public void ring(Vec3 pos, boolean depth, float radius, int color, int slices) {
        addTask(new Ring(pos, depth, radius, color, slices, DEFAULT_RING_LAYERS));
    }

    public void ring(Vec3 pos, boolean depth, float radius, int color, int slices, int layers) {
        addTask(new Ring(pos, depth, radius, color, slices, layers));
    }

    public void ring(Vec3 pos, boolean depth, float radius, Color color) {
        addTask(new Ring(pos, depth, radius, color.getARGB(), DEFAULT_SLICE_COUNT, DEFAULT_RING_LAYERS));
    }

    public void ring(Vec3 pos, boolean depth, float radius, Color color, int slices) {
        addTask(new Ring(pos, depth, radius, color.getARGB(), slices, DEFAULT_RING_LAYERS));
    }

    public void ring(Vec3 pos, boolean depth, float radius, Color color, int slices, int layers) {
        addTask(new Ring(pos, depth, radius, color.getARGB(), slices, layers));
    }

    public void text(String content, Vec3 pos) {
        text(content, pos, Color.WHITE.getARGB(), true, true);
    }

    public void text(String content, Vec3 pos, int color) {
        text(content, pos, color, true, true);
    }

    public void text(String content, Vec3 pos, int color, boolean depth) {
        text(content, pos, color, depth, true);
    }

    public void text(String content, Vec3 pos, int color, boolean depth, boolean dropShadow) {
        Font font = mc.font;
        Quaternionf rotation = mc.gameRenderer.mainCamera().rotation();
        addTask(new Text(content, color, pos, DEFAULT_TEXT_SCALE, rotation, font, font.width(content), depth, dropShadow));
    }

    public void text(String content, Vec3 pos, Color color) {
        text(content, pos, color.getARGB(), true, true);
    }

    public void text(String content, Vec3 pos, Color color, boolean depth) {
        text(content, pos, color.getARGB(), depth, true);
    }

    public void text(String content, Vec3 pos, Color color, boolean depth, boolean dropShadow) {
        text(content, pos, color.getARGB(), depth, dropShadow);
    }

    public void text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width) {
        addTask(new Text(content, Color.WHITE.getARGB(), pos, scale, rotation, font, width, true, true));
    }

    public void text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        addTask(new Text(content, Color.WHITE.getARGB(), pos, scale, rotation, font, width, depth, true));
    }

    public void text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        addTask(new Text(content, Color.WHITE.getARGB(), pos, scale, rotation, font, width, depth, dropShadow));
    }

    public void text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width) {
        addTask(new Text(content, color, pos, scale, rotation, font, width, true, true));
    }

    public void text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        addTask(new Text(content, color, pos, scale, rotation, font, width, depth, true));
    }

    public void text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        addTask(new Text(content, color, pos, scale, rotation, font, width, depth, dropShadow));
    }

    public void text(String content, Color color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width) {
        addTask(new Text(content, color.getARGB(), pos, scale, rotation, font, width, true, true));
    }

    public void text(String content, Color color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        addTask(new Text(content, color.getARGB(), pos, scale, rotation, font, width, depth, true));
    }

    public void text(String content, Color color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        addTask(new Text(content, color.getARGB(), pos, scale, rotation, font, width, depth, dropShadow));
    }


    private class TaskList<T extends RenderTask> {
        public final List<T> depth = new ArrayList<>();
        public final List<T> noDepth = new ArrayList<>();

        public void add(T task) {
            (task.isDepth() ? depth : noDepth).add(task);
        }

        public void clear() {
            depth.clear();
            noDepth.clear();
        }
    }
}
