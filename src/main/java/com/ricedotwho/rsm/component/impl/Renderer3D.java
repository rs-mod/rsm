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
    private final TaskSet<Beacon> beacons = new TaskSet<>();

    private final Map<Class<? extends RenderTask>, TaskSet<? extends RenderTask>> lineMap = new HashMap<>();
    private final Map<Class<? extends RenderTask>, TaskSet<? extends RenderTask>> filledMap = new HashMap<>();

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
    public <T extends RenderTask> TaskSet<T> getLineSet(Class<T> type) {
        return (TaskSet<T>) lineMap.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderTask> TaskSet<T> getFilledSet(Class<T> type) {
        return (TaskSet<T>) filledMap.get(type);
    }

    public static <T extends RenderTask> void registerLine(Class<T> type, TaskSet<T> set) {
        instance.lineMap.put(type, set);
    }

    public static <T extends RenderTask> void registerLine(Class<T> type) {
        registerLine(type, new TaskSet<T>());
    }

    public static <T extends RenderTask> void registerFilled(Class<T> type, TaskSet<T> set) {
        instance.filledMap.put(type, set);
    }

    public static <T extends RenderTask> void registerFilled(Class<T> type) {
        registerFilled(type, new TaskSet<T>());
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
        for (int i = 0; i < 2; ++i) {
            boolean depth = i == 0;

            Set<RenderTask> tasks = lineMap.values()
                    .stream()
                    .flatMap(taskSet -> taskSet.getDepth(depth).stream())
                    .collect(Collectors.toSet());

            if (tasks.isEmpty()) continue;

            RenderType.CompositeRenderType type = depth ? Render3DLayer.LINE_LIST : Render3DLayer.LINE_LIST_ESP;
            VertexConsumer buffer = source.getBuffer(type);

            tasks.forEach(t -> t.render(stack, buffer, com.ricedotwho.rsm.utils.render.render3d.type.RenderType.LINE));

            source.endBatch(type);
        }

    }

    private void renderBatchedFilled(MultiBufferSource.BufferSource source, PoseStack stack) {
        for (int i = 0; i < 2; ++i) {
            boolean depth = i == 0;

            Set<RenderTask> tasks = filledMap.values()
                    .stream()
                    .flatMap(taskSet -> taskSet.getDepth(depth).stream())
                    .collect(Collectors.toSet());

            if (tasks.isEmpty()) continue;

            RenderType.CompositeRenderType type = depth ? Render3DLayer.TRIANGLE_STRIP : Render3DLayer.TRIANGLE_STRIP_ESP;
            VertexConsumer buffer = source.getBuffer(type);

            tasks.forEach(t -> t.render(stack, buffer, com.ricedotwho.rsm.utils.render.render3d.type.RenderType.FILLED));

            source.endBatch(type);
        }

    }

    private void renderBatchedBeaconBeams(PoseStack stack, Vec3 camera) {
        for (Beacon task : this.beacons.set) {
            task.renderBeacon(stack, camera);
        }
    }

    /// Call this from {@link Render3DEvent.Extract} to avoid {@link ConcurrentModificationException}
    @SuppressWarnings("unchecked")
    public static <T extends RenderTask> void addTask(T task) {
        TaskSet<T> set;
        switch (task.getType()) {
            case LINE -> set = instance.getLineSet((Class<T>) task.getClass());
            case FILLED -> set = instance.getFilledSet((Class<T>) task.getClass());
            case FILLED_OUTLINE -> {
                instance.getLineSet((Class<T>) task.getClass()).set.add(task);
                instance.getFilledSet((Class<T>) task.getClass()).set.add(task);
                return;
            }
            case BEACON -> {
                instance.beacons.set.add((Beacon) task);
                return;
            }
            default -> {
                return;
            }
        }
        set.set.add(task);
    }

    public static class TaskSet<T extends RenderTask> {
        public final Set<T> set = new HashSet<>();

        public Set<T> getDepth(boolean state) {
            return this.set.stream().filter((e) -> e.isDepth() == state).collect(Collectors.toSet());
        }

        public void clear() {
            this.set.clear();
        }
    }
}
