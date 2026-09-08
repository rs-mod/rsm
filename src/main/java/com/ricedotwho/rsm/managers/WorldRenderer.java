package com.ricedotwho.rsm.managers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.render.render3d.Render3DLayer;
import com.ricedotwho.rsm.render.render3d.type.*;
import com.ricedotwho.rsm.type.Color;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Register
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
    private  <T extends RenderTask> TaskList<T> getLineList(Class<T> type) {
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
            Vec3 camera = mc.gameRenderer.getMainCamera().position();
            LevelRenderContext ctx = event.getContext();

            MultiBufferSource.BufferSource source = ctx.bufferSource();

            stack.pushPose();
            stack.translate(-camera.x(), -camera.y(), -camera.z());

            renderBatchedLines(source, stack);
            renderBatchedFilled(source, stack);

            stack.popPose();

            renderBatchedBeaconBeams(stack, camera);
            renderBatchedText(source, stack, camera);
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

    private void renderBatchedLines(MultiBufferSource.BufferSource source, PoseStack stack) {
        for (int i = 0; i < 2; i++) {
            boolean depth = i == 0;
            RenderType type = depth ? Render3DLayer.LINE_LIST : Render3DLayer.LINE_LIST_ESP;

            VertexConsumer buffer = source.getBuffer(type);
            boolean rendered = false;

            for (TaskList<? extends RenderTask> taskSet : lineMap.values()) {
                List<? extends RenderTask> list = depth ? taskSet.depth : taskSet.noDepth;
                for (RenderTask task : list) {
                    task.render(stack, buffer, com.ricedotwho.rsm.render.render3d.type.RenderType.LINE);
                    rendered = true;
                }
            }

            if (rendered) {
                source.endBatch(type);
            }
        }
    }

    private void renderBatchedFilled(MultiBufferSource.BufferSource source, PoseStack stack) {
        for (int i = 0; i < 2; i++) {
            boolean depth = i == 0;
            RenderType type = depth ? Render3DLayer.TRIANGLE_STRIP : Render3DLayer.TRIANGLE_STRIP_ESP;

            VertexConsumer buffer = source.getBuffer(type);
            boolean rendered = false;

            for (TaskList<? extends RenderTask> taskList : filledMap.values()) {
                List<? extends RenderTask> list = depth ? taskList.depth : taskList.noDepth;

                for (RenderTask task : list) {
                    task.render(stack, buffer, com.ricedotwho.rsm.render.render3d.type.RenderType.FILLED);
                    rendered = true;
                }
            }

            if (rendered) {
                source.endBatch(type);
            }
        }
    }

    private void renderBatchedText(MultiBufferSource.BufferSource source, PoseStack stack, Vec3 camera) {
        Vec3 cameraPos = camera.scale(-1);
        for (Text task : texts) {
            stack.pushPose();
            Matrix4f pose = stack.last().pose();
            float scale = task.getScale() * 0.025f;
            pose.translate(task.getPos().toVector3f())
                    .translate(cameraPos.toVector3f())
                    .rotate(task.getRotation())
                    .scale(scale, -scale, scale);

            task.getFont().drawInBatch(task.getContent(), -task.getWidth() / 2f, 0, task.getColor(), task.isDropShadow(), pose, source,
                    task.isDepth() ? Font.DisplayMode.POLYGON_OFFSET : Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightCoordsUtil.FULL_BRIGHT
            );

            stack.popPose();
        }
    }

    private void renderBatchedBeaconBeams(PoseStack stack, Vec3 camera) {
        for (Beacon task : beacons) {
            task.renderBeacon(stack, camera);
        }
    }

    /// Call this from {@link Render3DEvent.Extract} to avoid {@link ConcurrentModificationException}
    @SuppressWarnings("unchecked")
    <T extends RenderTask> void addTask(T task) {
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

    private static final float DEFAULT_LINE_WIDTH = 3f;
    private static final int DEFAULT_RING_SLICES = 64;
    private static final int DEFAULT_RING_LAYERS = 16;
    private static final float DEFAULT_TEXT_SCALE = 1f;
    private static final boolean DEFAULT_DROP_SHADOW = true;

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

    public void filledBox(AABB aabb, int color, boolean depth) {
        addTask(new FilledBox(aabb, color, depth));
    }

    public void filledBox(BlockPos pos, int color, boolean depth) {
        filledBox(new AABB(pos), color, depth);
    }

    public void filledBox(AABB aabb, Color color, boolean depth) {
        filledBox(aabb, color.getARGB(), depth);
    }

    public void filledBox(BlockPos pos, Color color, boolean depth) {
        filledBox(new AABB(pos), color.getARGB(), depth);
    }

    public void filledOutlineBox(AABB aabb, int fill, int line, boolean depth) {
        filledOutlineBox(aabb, fill, line, depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, int fill, int line, boolean depth, float width) {
        addTask(new FilledOutlineBox(aabb, fill, line, depth, width));
    }

    public void filledOutlineBox(AABB aabb, Color fill, Color line, boolean depth) {
        filledOutlineBox(aabb, fill.getARGB(), line.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(AABB aabb, Color fill, Color line, boolean depth, float width) {
        filledOutlineBox(aabb, fill.getARGB(), line.getARGB(), depth, width);
    }

    public void filledOutlineBox(BlockPos pos, int fill, int line, boolean depth) {
        filledOutlineBox(new AABB(pos), fill, line, depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, int fill, int line, boolean depth, float width) {
        filledOutlineBox(new AABB(pos), fill, line, depth, width);
    }

    public void filledOutlineBox(BlockPos pos, Color fill, Color line, boolean depth) {
        filledOutlineBox(new AABB(pos), fill.getARGB(), line.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, Color fill, Color line, boolean depth, float width) {
        filledOutlineBox(new AABB(pos), fill.getARGB(), line.getARGB(), depth, width);
    }

    public void filledOutlineBox(AABB aabb, Color color, boolean depth) {
        filledOutlineBox(aabb, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineBox(BlockPos pos, Color color, boolean depth) {
        filledOutlineBox(new AABB(pos), color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH);
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int fill, int line, boolean depth) {
        addTask(new FilledOutlineShape(pos, shape, fill, line, depth, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, int fill, int line, boolean depth, float width) {
        addTask(new FilledOutlineShape(pos, shape, fill, line, depth, width));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line, boolean depth) {
        addTask(new FilledOutlineShape(pos, shape, fill.getARGB(), line.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line, boolean depth, float width) {
        addTask(new FilledOutlineShape(pos, shape, fill.getARGB(), line.getARGB(), depth, width));
    }

    public void filledShape(BlockPos pos, VoxelShape shape, int color, boolean depth) {
        addTask(new FilledShape(pos, shape, color, depth));
    }

    public void filledShape(BlockPos pos, VoxelShape shape, Color color, boolean depth) {
        addTask(new FilledShape(pos, shape, color.getARGB(), depth));
    }

    public void line(Vec3 from, Vec3 to, int start, int end, boolean depth) {
        addTask(new Line(from, to, start, end, depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, int start, int end, boolean depth, float width) {
        addTask(new Line(from, to, start, end, depth, width));
    }

    public void line(Vec3 from, Vec3 to, Color start, Color end, boolean depth) {
        addTask(new Line(from, to, start.getARGB(), end.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, Color start, Color end, boolean depth, float width) {
        addTask(new Line(from, to, start.getARGB(), end.getARGB(), depth, width));
    }

    public void line(Vec3 from, Vec3 to, int color, boolean depth) {
        addTask(new Line(from, to, color, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, Color color, boolean depth) {
        addTask(new Line(from, to, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void line(Vec3 from, Vec3 to, int color, boolean depth, float width) {
        addTask(new Line(from, to, color, color, depth, width));
    }

    public void line(Vec3 from, Vec3 to, Color color, boolean depth, float width) {
        addTask(new Line(from, to, color.getARGB(), color.getARGB(), depth, width));
    }

    public void lineList(List<Vec3> positions, int start, int end, boolean depth) {
        addTask(new LineList(positions, start, end, depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, int start, int end, boolean depth, float width) {
        addTask(new LineList(positions, start, end, depth, width));
    }

    public void lineList(List<Vec3> positions, Color start, Color end, boolean depth) {
        addTask(new LineList(positions, start.getARGB(), end.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, Color start, Color end, boolean depth, float width) {
        addTask(new LineList(positions, start.getARGB(), end.getARGB(), depth, width));
    }

    public void lineList(List<Vec3> positions, int color, boolean depth) {
        addTask(new LineList(positions, color, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, Color color, boolean depth) {
        addTask(new LineList(positions, color.getARGB(), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void lineList(List<Vec3> positions, int color, boolean depth, float width) {
        addTask(new LineList(positions, color, color, depth, width));
    }

    public void lineList(List<Vec3> positions, Color color, boolean depth, float width) {
        addTask(new LineList(positions, color.getARGB(), color.getARGB(), depth, width));
    }


    public void outlineBox(AABB aabb, int color, boolean depth) {
        addTask(new OutlineBox(aabb, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(AABB aabb, int color, boolean depth, float width) {
        addTask(new OutlineBox(aabb, color, depth, width));
    }

    public void outlineBox(AABB aabb, Color color, boolean depth) {
        addTask(new OutlineBox(aabb, color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(AABB aabb, Color color, boolean depth, float width) {
        addTask(new OutlineBox(aabb, color.getARGB(), depth, width));
    }

    public void outlineBox(BlockPos pos, int color, boolean depth) {
        addTask(new OutlineBox(new AABB(pos), color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(BlockPos pos, Color color, boolean depth) {
        addTask(new OutlineBox(new AABB(pos), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(Vec3 pos, double scale, int color, boolean depth) {
        addTask(new OutlineBox(cubeAround(pos, scale), color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineBox(Vec3 pos, double scale, Color color, boolean depth) {
        addTask(new OutlineBox(cubeAround(pos, scale), color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }


    public void outlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth) {
        addTask(new OutlineShape(pos, shape, color, depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth, float width) {
        addTask(new OutlineShape(pos, shape, color, depth, width));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth) {
        addTask(new OutlineShape(pos, shape, color.getARGB(), depth, DEFAULT_LINE_WIDTH));
    }

    public void outlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth, float width) {
        addTask(new OutlineShape(pos, shape, color.getARGB(), depth, width));
    }


    public void rectangle(AABB aabb, int color, boolean depth) {
        addTask(new Rectangle(aabb, color, DEFAULT_LINE_WIDTH, depth));
    }

    public void rectangle(AABB aabb, int color, float lineWidth, boolean depth) {
        addTask(new Rectangle(aabb, color, lineWidth, depth));
    }

    public void rectangle(AABB aabb, Color color, boolean depth) {
        addTask(new Rectangle(aabb, color.getARGB(), DEFAULT_LINE_WIDTH, depth));
    }

    public void rectangle(AABB aabb, Color color, float lineWidth, boolean depth) {
        addTask(new Rectangle(aabb, color.getARGB(), lineWidth, depth));
    }


    public void ring(Vec3 pos, boolean depth, float radius, int color) {
        addTask(new Ring(pos, depth, radius, color, DEFAULT_RING_SLICES, DEFAULT_RING_LAYERS));
    }

    public void ring(Vec3 pos, boolean depth, float radius, int color, int slices, int layers) {
        addTask(new Ring(pos, depth, radius, color, slices, layers));
    }

    public void ring(Vec3 pos, boolean depth, float radius, Color color) {
        addTask(new Ring(pos, depth, radius, color.getARGB(), DEFAULT_RING_SLICES, DEFAULT_RING_LAYERS));
    }

    public void ring(Vec3 pos, boolean depth, float radius, Color color, int slices, int layers) {
        addTask(new Ring(pos, depth, radius, color.getARGB(), slices, layers));
    }


    public void text(String content, Vec3 pos, boolean depth) {
        text(content, Color.WHITE.getARGB(), pos, depth, DEFAULT_DROP_SHADOW);
    }

    public void text(String content, Vec3 pos, boolean depth, boolean dropShadow) {
        text(content, Color.WHITE.getARGB(), pos, depth, dropShadow);
    }

    public void text(String content, int color, Vec3 pos, boolean depth) {
        text(content, color, pos, depth, DEFAULT_DROP_SHADOW);
    }

    public void text(String content, int color, Vec3 pos, boolean depth, boolean dropShadow) {
        Font font = mc.font;
        Quaternionf rotation = mc.gameRenderer.getMainCamera().rotation();
        addTask(new Text(content, color, pos, DEFAULT_TEXT_SCALE, rotation, font, font.width(content), depth, dropShadow));
    }

    public void text(String content, Color color, Vec3 pos, boolean depth, boolean dropShadow) {
        text(content, color.getARGB(), pos, depth, dropShadow);
    }

    public void text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        addTask(new Text(content, Color.WHITE.getARGB(), pos, scale, rotation, font, width, depth, DEFAULT_DROP_SHADOW));
    }

    public void text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        addTask(new Text(content, Color.WHITE.getARGB(), pos, scale, rotation, font, width, depth, dropShadow));
    }

    public void text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        addTask(new Text(content, color, pos, scale, rotation, font, width, depth, DEFAULT_DROP_SHADOW));
    }

    public void text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        addTask(new Text(content, color, pos, scale, rotation, font, width, depth, dropShadow));
    }

    public void text(String content, Color color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        addTask(new Text(content, color.getARGB(), pos, scale, rotation, font, width, depth, DEFAULT_DROP_SHADOW));
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
