package com.ricedotwho.rsm.event.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.render.render3d.Render3DLayer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

import static com.ricedotwho.rsm.type.Accessor.mc;

public sealed abstract class Render3DEvent extends Event {
    private static final float DEFAULT_LINE_WIDTH = 3f;
    private static final float DEFAULT_RADIUS = 0.5F;
    private static final int DEFAULT_RING_LAYERS = 16;
    private static final int DEFAULT_SLICE_COUNT = 48;
    private static final float DEFAULT_TEXT_SCALE = 1f;

    @Getter
    @AllArgsConstructor
    public final static class Extract extends Render3DEvent {
        private final LevelRenderContext context;

        public void text(String content, Vec3 pos, float scale = DEFAULT_TEXT_SCALE, boolean depth = false) {
            textC(Component.literal(content), pos, scale, depth);
        }

        public void textC(Component content, Vec3 pos, float scale = DEFAULT_TEXT_SCALE, boolean depth = false) {
            var scaleFactor = scale * 0.025f;
            var displayMode = depth ? Font.DisplayMode.POLYGON_OFFSET : Font.DisplayMode.SEE_THROUGH;
            var x = -mc.font.width(content) / 2f;
            var stack = context.poseStack();
            stack.pushPose();
            stack.last().pose()
                    .translate(pos.toVector3f())
                    .translate(mc.gameRenderer.mainCamera().position().toVector3f().mul(-1))
                    .rotate(mc.gameRenderer.mainCamera().rotation())
                    .scale(scaleFactor, -scaleFactor, scaleFactor);

            context.submitNodeCollector().submitText(stack, x, 0f, content.getVisualOrderText(), true, displayMode, LightCoordsUtil.FULL_BRIGHT, -1, 0, 0);
            stack.popPose();
        }

        public void beacon(BlockPos pos, Color colour) {
            var camera = mc.gameRenderer.mainCamera().position();
            var scale = mc.player == null || mc.player.isScoping() ? 1f : (float) Math.max(1f, camera.subtract(Vec3.atCenterOf(pos)).horizontalDistance() / 96f);
            var animationTime = (mc.level == null ? 0 : mc.level.getGameTime() % 40) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            var stack = context.poseStack();

            BeaconRenderer.submitBeaconBeam(
                    stack, context.submitNodeCollector(), BeaconRenderer.BEAM_LOCATION, 1f,
                    animationTime, 0, BeaconRenderer.MAX_RENDER_Y, colour.getARGB(),
                    BeaconRenderer.SOLID_BEAM_RADIUS * scale, BeaconRenderer.BEAM_GLOW_RADIUS * scale
            );
            stack.popPose();
        }

        public void circle(Vec3 pos, Color colour, boolean depth = false, float radius = 0.5f, int slices = DEFAULT_SLICE_COUNT, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            context.submitNodeCollector().submitCustomGeometry(context.poseStack(), resolveLineRenderType(depth, colour.isFullyOpaque()), (pose, buffer) ->
                    VertexRenderer.renderCircle(
                            pose,
                            buffer,
                            pos,
                            radius,
                            colour.getARGB(),
                            slices,
                            width
                    )
            );
            context.poseStack().popPose();
        }

        public void filled(BlockPos pos, Color colour, boolean depth) {
            filled(new AABB(pos), colour, depth);
        }

        public void filled(AABB box, Color colour, boolean depth) {
            Render3DEvent.translateCamera(context);
            context.submitNodeCollector().submitCustomGeometry(context.poseStack(), resolveQuadRenderType(depth, colour.getAlpha() >= 0.999f), (pose, buffer) -> {
                VertexRenderer.addFilledBoxVertices(
                        pose,
                        buffer,
                        box,
                        colour.getARGB()
                );
            });
            context.poseStack().popPose();
        }

        public void filledOutline(BlockPos pos, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            filledOutline(new AABB(pos), fill, line, depth, width);
        }

        public void filledOutline(AABB box, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();

            context.submitNodeCollector().submitCustomGeometry(stack, resolveQuadRenderType(depth, fill.getAlpha() >= 0.999f), (pose, buffer) -> {
                VertexRenderer.addFilledBoxVertices(
                        pose,
                        buffer,
                        box,
                        fill.getARGB()
                );
            });

            // line
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, line.getAlpha() >= 0.999f), (pose, buffer) -> {
                VertexRenderer.renderOutlineBox(
                        pose,
                        buffer,
                        box,
                        line.getARGB(),
                        width
                );
            });
            stack.popPose();
        }

        public void outline(BlockPos pos, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            outline(new AABB(pos), line, depth, width);
        }

        public void outline(AABB box, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();

            // line
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, line.getAlpha() >= 0.999f), (pose, buffer) -> {
                VertexRenderer.renderOutlineBox(
                        pose,
                        buffer,
                        box,
                        line.getARGB(),
                        width
                );
            });
            stack.popPose();
        }

        public void filledShape(BlockPos pos, Color fill, boolean depth = false) {
            filledShape(pos, pos.getShape(), fill, depth);
        }

        public void filledShape(BlockPos pos, VoxelShape shape, Color fill, boolean depth = false) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveQuadRenderType(depth, fill.getAlpha() >= 0.999f), (pose, buffer) -> {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    VertexRenderer.addFilledBoxVertices(
                            pose,
                            buffer,
                            pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                            fill.getARGB()
                    );
                });
            });
            stack.popPose();
        }

        public void filledOutlineShape(BlockPos pos, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            filledOutlineShape(pos, pos.getShape(), fill, line, depth, width);
        }

        public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveQuadRenderType(depth, fill.getAlpha() >= 0.999f), (pose, buffer) -> {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    VertexRenderer.addFilledBoxVertices(
                            pose,
                            buffer,
                            pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                            fill.getARGB()
                    );
                });
            });

            // line
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, line.getAlpha() >= 0.999f), (pose, buffer) -> {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    VertexRenderer.renderOutlineBox(
                            pose,
                            buffer,
                            pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                            line.getARGB(),
                            width
                    );
                });
            });
            stack.popPose();
        }

        public void outlineShape(BlockPos pos, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            outlineShape(pos, pos.getShape(), line, depth, width);
        }

        public void outlineShape(BlockPos pos, VoxelShape shape, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, line.getAlpha() >= 0.999f), (pose, buffer) -> {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    VertexRenderer.renderOutlineBox(
                            pose,
                            buffer,
                            pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                            line.getARGB(),
                            width
                    );
                });
            });
            stack.popPose();
        }

        public void line(Vec3 from, Vec3 to, Color start, Color end, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, start.isFullyOpaque() && end.isFullyOpaque()), (pose, buffer) -> {
                VertexRenderer.renderLine(
                        pose,
                        buffer,
                        from,
                        to.subtract(from),
                        start.getARGB(),
                        end.getARGB(),
                        width
                );
            });
            stack.popPose();
        }

        public void lines(List<Vec3> lines, Color start, Color end, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, start.isFullyOpaque() && end.isFullyOpaque()), (pose, buffer) -> {
                int size = lines.size() - 1;
                int s = start.getARGB(), e = end.getARGB();
                for (int i = 0; i < size; i++) {
                    Vec3 f = lines.get(i);
                    Vec3 d = lines.get(i + 1).subtract(f);
                    float t0 = (float) i / size;
                    float t1 = (float) (i + 1) / size;
                    int c0 = Color.lerpARGB(s, e, t0);
                    int c1 = Color.lerpARGB(s, e, t1);
                    VertexRenderer.renderLine(
                            pose,
                            buffer,
                            f,
                            d,
                            c0,
                            c1,
                            width
                    );
                }
            });
            stack.popPose();
        }

        public void rect(AABB box, Color colour, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, colour.isFullyOpaque()), (pose, buffer) -> {
                VertexRenderer.renderHorizontalRect(
                        pose,
                        buffer,
                        box,
                        width,
                        colour.getARGB()
                );
            });
            stack.popPose();
        }

        public void ring(Vec3 pos, Color colour, boolean depth = false, float radius = DEFAULT_RADIUS, int slices = DEFAULT_SLICE_COUNT, int layers = DEFAULT_RING_LAYERS) {
            Render3DEvent.translateCamera(context);
            var stack = context.poseStack();
            context.submitNodeCollector().submitCustomGeometry(stack, resolveLineRenderType(depth, colour.isFullyOpaque()), (pose, buffer) -> {
                int factor = ringQuality(pos);
                if (factor == 0) return;
                int s = slices / factor;
                int l = layers / factor;
                VertexRenderer.renderRing(
                        stack.last(),
                        buffer,
                        pos,
                        radius,
                        colour.getARGB(),
                        s,
                        l
                );
            });
            stack.popPose();
        }

        private int ringQuality(Vec3 pos) {
            Entity camera = Minecraft.getInstance().getCameraEntity();
            assert camera != null;
            double dist = camera.distanceToSqr(pos);
            if (dist > 64 * 64) {
                return 0;
            } else if (dist > 48 * 48) {
                return 8;
            } else if (dist > 32 * 32) {
                return 4;
            } else if (dist > 16 *16) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public final static class Last extends Render3DEvent {
        private final LevelRenderContext context;
    }

    @Getter
    @AllArgsConstructor
    public final static class Start extends Render3DEvent {
        private final LevelTerrainRenderContext context;
    }

    private static RenderType resolveQuadRenderType(boolean depth, boolean fullyOpaque) {
        if (depth && fullyOpaque) return Render3DLayer.QUADS_OPAQUE;
        if (depth) return Render3DLayer.QUADS_TRANSLUCENT;
        if (fullyOpaque) return Render3DLayer.QUADS_ESP;
        return Render3DLayer.QUADS_TRANSLUCENT_ESP;
    }

    private static RenderType resolveLineRenderType(boolean depth, boolean fullyOpaque) {
        if (depth && fullyOpaque) return RenderTypes.LINES;
        if (depth) return Render3DLayer.LINES_TRANSLUCENT_DEPTH;
        if (fullyOpaque) return Render3DLayer.LINES_ESP;
        return Render3DLayer.LINES_TRANSLUCENT_ESP;
    }

    private static void translateCamera(LevelRenderContext ctx) {
        PoseStack stack = ctx.poseStack();
        Vec3 camera = mc.gameRenderer.mainCamera().position();
        stack.pushPose();
        stack.translate(-camera.x(), -camera.y(), -camera.z());
    }
}
