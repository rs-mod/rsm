package com.ricedotwho.rsm.component.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.utils.render.render3d.Render3DLayer;
import com.ricedotwho.rsm.utils.render.render3d.type.*;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class Renderer3D extends ModComponent {
    private static Renderer3D instance;
    private final List<Beacon> beacons = new ArrayList<>();

    private final Map<Class<? extends RenderTask>, TaskList<? extends RenderTask>> lineMap = new HashMap<>();
    private final Map<Class<? extends RenderTask>, TaskList<? extends RenderTask>> filledMap = new HashMap<>();

    public Renderer3D() {
        super("Renderer3D");
        instance = this;

        // Filled
        registerFilled(FilledBox.class);
        registerFilled(FilledOutlineBox.class);

        // Lines
        registerLine(Circle.class);
        registerLine(FilledOutlineBox.class);
        registerLine(Line.class);
        registerLine(OutlineBox.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderTask> TaskList<T> getLineList(Class<T> type) {
        return (TaskList<T>) lineMap.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderTask> TaskList<T> getFilledList(Class<T> type) {
        return (TaskList<T>) filledMap.get(type);
    }

    public static <T extends RenderTask> void registerLine(Class<T> type, TaskList<T> list) {
        instance.lineMap.put(type, list);
    }

    public static <T extends RenderTask> void registerLine(Class<T> type) {
        registerLine(type, new TaskList<T>());
    }

    public static <T extends RenderTask> void registerFilled(Class<T> type, TaskList<T> list) {
        instance.filledMap.put(type, list);
    }

    public static <T extends RenderTask> void registerFilled(Class<T> type) {
        registerFilled(type, new TaskList<T>());
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent.Last event) {
        PoseStack stack = event.getContext().matrices();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        WorldRenderContext ctx = event.getContext();

        MultiBufferSource buffer = ctx.consumers();
        if (!(buffer instanceof MultiBufferSource.BufferSource source)) return;

        stack.pushPose();
        stack.translate(-camera.x(), -camera.y(), -camera.z());

        this.renderBatchedLines(source, stack);
        this.renderBatchedFilled(source, stack);

        stack.popPose();

        this.renderBatchedBeaconBeams(stack, camera);

        this.clear();
    }

    private void clear() {
        lineMap.forEach((k, e) -> e.clear());
        filledMap.forEach((k, e) -> e.clear());
        beacons.clear();
    }

    private void renderBatchedLines(MultiBufferSource.BufferSource source, PoseStack stack) {
        for (int i = 0; i < 2; i++) {
            boolean depth = i == 0;
            RenderType.CompositeRenderType type = depth ? Render3DLayer.LINE_LIST : Render3DLayer.LINE_LIST_ESP;

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
            RenderType.CompositeRenderType type = depth ? Render3DLayer.TRIANGLE_STRIP : Render3DLayer.TRIANGLE_STRIP_ESP;

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

    private void renderBatchedBeaconBeams(PoseStack stack, Vec3 camera) {
        for (Beacon task : this.beacons) {
            task.renderBeacon(stack, camera);
        }
    }

    /// Call this from {@link Render3DEvent.Extract} to avoid {@link ConcurrentModificationException}
    @SuppressWarnings("unchecked")
    public static <T extends RenderTask> void addTask(T task) {
        TaskList<T> set;
        switch (task.getType()) {
            case LINE -> set = instance.getLineList((Class<T>) task.getClass());
            case FILLED -> set = instance.getFilledList((Class<T>) task.getClass());
            case FILLED_OUTLINE -> {
                instance.getLineList((Class<T>) task.getClass()).add(task);
                instance.getFilledList((Class<T>) task.getClass()).add(task);
                return;
            }
            case BEACON -> {
                instance.beacons.add((Beacon) task);
                return;
            }
            default -> {
                return;
            }
        }
        set.add(task);
    }

    public static class TaskList<T extends RenderTask> {
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
