package com.ricedotwho.rsm.component.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.mixins.accessor.AccessorBeaconBeam;
import com.ricedotwho.rsm.utils.render.render3d.Render3DLayer;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.utils.render.render3d.type.*;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Renderer3D extends ModComponent {
    private static Renderer3D instance;
    private final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    private final TaskSet<BeaconTask> beacons = new TaskSet<>();
    private final TaskSet<FilledBoxTask> filledBoxes = new TaskSet<>();
    private final TaskSet<FilledOutlineBoxTask> filledOutlineBoxes = new TaskSet<>();
    private final TaskSet<OutlineBoxTask> outlineBoxes = new TaskSet<>();
    private final TaskSet<LineTask> lines = new TaskSet<>();
    private final TaskSet<CircleTask> circles = new TaskSet<>();

    public Renderer3D() {
        super("Renderer3D");
        instance = this;
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
        this.beacons.clear();
        this.filledBoxes.clear();
        this.filledOutlineBoxes.clear();
        this.outlineBoxes.clear();
        this.lines.clear();
    }

    private void renderBatchedLines(MultiBufferSource.BufferSource source, PoseStack stack) {
        for (int i = 0; i < 2; ++i) {
            boolean depth = i == 0;
            Set<LineTask> lineTasks = this.lines.getDepth(depth);
            Set<OutlineBoxTask> outlineBoxTasks = this.outlineBoxes.getDepth(depth);
            Set<FilledOutlineBoxTask> filledOutlineBoxTasks = this.filledOutlineBoxes.getDepth(depth);
            Set<CircleTask> circleTasks = this.circles.getDepth(depth);

            if (lineTasks.isEmpty() && outlineBoxTasks.isEmpty() && filledOutlineBoxTasks.isEmpty() && circleTasks.isEmpty()) continue;

            RenderType.CompositeRenderType type = depth ? Render3DLayer.LINE_LIST : Render3DLayer.LINE_LIST_ESP;
            VertexConsumer buffer = source.getBuffer(type);

            for (LineTask task : lineTasks) {
                VertexRenderer.renderLine(
                        stack.last(),
                        buffer,
                        task.getFrom(),
                        task.getTo().subtract(task.getFrom()),
                        task.getStart(),
                        task.getEnd()
                );
            }

            for (OutlineBoxTask task : outlineBoxTasks) {
                VertexRenderer.renderOutlineBox(
                        stack.last(),
                        buffer,
                        task.getAabb(),
                        task.getColour()
                );
            }

            for (FilledOutlineBoxTask task : filledOutlineBoxTasks) {
                VertexRenderer.renderOutlineBox(
                        stack.last(),
                        buffer,
                        task.getAabb(),
                        task.getLine()
                );
            }

            for (CircleTask task : circleTasks) {
                VertexRenderer.renderCircle(
                        stack.last(),
                        buffer,
                        task.getPos(),
                        task.getRadius(),
                        task.getColour(),
                        task.getSlices()
                );
            }

            source.endBatch(type);
        }

    }

    private void renderBatchedFilled(MultiBufferSource.BufferSource source, PoseStack stack) {
        for (int i = 0; i < 2; ++i) {
            boolean depth = i == 0;
            Set<FilledBoxTask> boxTasks = this.filledBoxes.getDepth(depth);
            Set<FilledOutlineBoxTask> outlinedBoxTasks = this.filledOutlineBoxes.getDepth(depth);

            if (boxTasks.isEmpty() && outlinedBoxTasks.isEmpty()) continue;

            RenderType.CompositeRenderType type = depth ? Render3DLayer.TRIANGLE_STRIP : Render3DLayer.TRIANGLE_STRIP_ESP;
            VertexConsumer buffer = source.getBuffer(type);

            for (FilledBoxTask task : boxTasks) {
                VertexRenderer.addFilledBoxVertices(
                        stack.last(),
                        buffer,
                        task.getAabb(),
                        task.getColour()
                );
            }

            for (FilledOutlineBoxTask task : outlinedBoxTasks) {
                VertexRenderer.addFilledBoxVertices(
                        stack.last(),
                        buffer,
                        task.getAabb(),
                        task.getFill()
                );
            }

            source.endBatch(type);
        }

    }

    private void renderBatchedBeaconBeams(PoseStack stack, Vec3 camera) {
        for (BeaconTask task : this.beacons.set) {
            stack.pushPose();
            stack.translate(task.getPos().x() - camera.x(), task.getPos().y() - camera.y(), task.getPos().z() - camera.z());
            double cX = task.getPos().x() + (double)0.5F;
            double cZ = task.getPos().z() + (double)0.5F;
            double dx = camera.x() - cX;
            double dz = camera.z() - cZ;
            float length = (float)Math.sqrt(dx * dx + dz * dz);
            float scale = task.isScoping() ? 1.0F : Math.max(1.0F, length * 0.010416667F);
            AccessorBeaconBeam.renderBeaconBeam(
                    stack,
                    mc.gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage(),
                    this.BEAM_TEXTURE,
                    1.0F,
                    task.getGameTime(),
                    0,
                    319,
                    task.getColour().getRGB(),
                    0.2F * scale,
                    0.25F * scale
            );
            stack.popPose();
        }
    }

    //todo: allow addons to register RenderTasks?
    /// Call this from {@link Render3DEvent.Extract} to avoid {@link ConcurrentModificationException}
    public static void addTask(RenderTask task) {
        switch (task.getType()) {
            case LINE -> instance.lines.set.add((LineTask)task);
            case BEACON -> instance.beacons.set.add((BeaconTask)task);
            case CIRCLE -> instance.circles.set.add((CircleTask)task);
            case BOX_FILLED -> instance.filledBoxes.set.add((FilledBoxTask)task);
            case BOX_OUTLINE -> instance.outlineBoxes.set.add((OutlineBoxTask)task);
            case BOX_FILLED_OUTLINE -> instance.filledOutlineBoxes.set.add((FilledOutlineBoxTask)task);
        }

    }

    private static class TaskSet<T extends RenderTask> {
        public final Set<T> set = new HashSet<>();

        public Set<T> getDepth(boolean state) {
            return this.set.stream().filter((e) -> e.isDepth() == state).collect(Collectors.toSet());
        }

        public void clear() {
            this.set.clear();
        }
    }
}
