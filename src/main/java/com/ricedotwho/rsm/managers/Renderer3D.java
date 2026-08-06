package com.ricedotwho.rsm.managers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.utils.render.render3d.Render3DLayer;
import com.ricedotwho.rsm.utils.render.render3d.type.*;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;

import static com.ricedotwho.rsm.utils.Accessor.mc;

@Register
@UtilityClass
public class Renderer3D{
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
    public <T extends RenderTask> TaskList<T> getLineList(Class<T> type) {
        return (TaskList<T>) lineMap.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderTask> TaskList<T> getFilledList(Class<T> type) {
        return (TaskList<T>) filledMap.get(type);
    }

    public <T extends RenderTask> void registerLine(Class<T> type, TaskList<T> list) {
        lineMap.put(type, list);
    }

    public <T extends RenderTask> void registerLine(Class<T> type) {
        registerLine(type, new TaskList<>());
    }

    public <T extends RenderTask> void registerFilled(Class<T> type, TaskList<T> list) {
        filledMap.put(type, list);
    }

    public <T extends RenderTask> void registerFilled(Class<T> type) {
        registerFilled(type, new TaskList<>());
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent.Last event) {
        PoseStack stack = event.getContext().poseStack();
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        LevelRenderContext ctx = event.getContext();

        MultiBufferSource.BufferSource source = ctx.bufferSource();
        //if (!(buffer instanceof MultiBufferSource.BufferSource source)) return; // removed in 26.1

        stack.pushPose();
        stack.translate(-camera.x(), -camera.y(), -camera.z());

        renderBatchedLines(source, stack);
        renderBatchedFilled(source, stack);

        stack.popPose();

        renderBatchedBeaconBeams(stack, camera);
        renderBatchedText(source, stack, camera);

        clear();
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
                    task.render(stack, buffer, com.ricedotwho.rsm.utils.render.render3d.type.RenderType.LINE);
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
                    task.render(stack, buffer, com.ricedotwho.rsm.utils.render.render3d.type.RenderType.FILLED);
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

            task.getFont().drawInBatch(task.getContent(), -task.getWidth() / 2f, 0, -1, true, pose, source,
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
    public <T extends RenderTask> void addTask(T task) {
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

    public class TaskList<T extends RenderTask> {
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
