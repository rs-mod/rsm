package com.ricedotwho.rsm.event.impl.render;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.render.render3d.WorldRenderer;
import com.ricedotwho.rsm.type.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

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

        public void text(String content, Vec3 pos, boolean depth = false, Color color = Color.WHITE, Color outline = Color.TRANSPARENT, boolean shadow = false, float scale = DEFAULT_TEXT_SCALE) {
            textC(Component.literal(content), pos, depth, color, outline, shadow, scale);
        }

        public void textC(Component content, Vec3 pos, boolean depth = false, Color color = Color.WHITE, Color outline = Color.TRANSPARENT, boolean shadow = false, float scale = DEFAULT_TEXT_SCALE) {
            WorldRenderer.text(context, content, pos, depth, color, outline, shadow, scale);
        }

        public void beacon(Vec3 pos, Color colour) {
            WorldRenderer.beacon(context, pos, colour);
        }

        public void beacon(Vec3i pos, Color colour) {
            beacon(Vec3.atLowerCornerOf(pos), colour);
        }

        public void circle(Vec3 pos, Color colour, boolean depth = false, float radius = 0.5f, int slices = DEFAULT_SLICE_COUNT, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.circle(context, pos, colour, depth, radius, slices, width);
        }

        public void filled(BlockPos pos, Color colour, boolean depth) {
            filled(new AABB(pos), colour, depth);
        }

        public void filled(AABB box, Color colour, boolean depth) {
            WorldRenderer.filled(context, box, colour, depth);
        }

        public void filledOutline(BlockPos pos, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            filledOutline(new AABB(pos), fill, line, depth, width);
        }

        public void filledOutline(AABB box, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.filledOutline(context, box, fill, line, depth, width);
        }

        public void outline(BlockPos pos, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            outline(new AABB(pos), line, depth, width);
        }

        public void outline(AABB box, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.outline(context, box, line, depth, width);
        }

        public void filledShape(BlockPos pos, Color fill, boolean depth = false) {
            filledShape(pos, pos.getShape(), fill, depth);
        }

        public void filledShape(BlockPos pos, VoxelShape shape, Color fill, boolean depth = false) {
            WorldRenderer.filledShape(context, pos, shape, fill, depth);
        }

        public void filledOutlineShape(BlockPos pos, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            filledOutlineShape(pos, pos.getShape(), fill, line, depth, width);
        }

        public void filledOutlineShape(BlockPos pos, VoxelShape shape, Color fill, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.filledOutlineShape(context, pos, shape, fill, line, depth, width);
        }

        public void outlineShape(BlockPos pos, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            outlineShape(pos, pos.getShape(), line, depth, width);
        }

        public void outlineShape(BlockPos pos, VoxelShape shape, Color line, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.outlineShape(context, pos, shape, line, depth, width);
        }

        public void line(Vec3 from, Vec3 to, Color start, Color end, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.line(context, from, to,start, end, depth, width);
        }

        public void lines(List<Vec3> lines, Color start, Color end, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.lines(context, lines, start, end, depth, width);
        }

        public void rect(AABB box, Color colour, boolean depth = false, float width = DEFAULT_LINE_WIDTH) {
            WorldRenderer.rect(context, box, colour, depth, width);
        }

        public void ring(Vec3 pos, Color colour, boolean depth = false, float radius = DEFAULT_RADIUS, int slices = DEFAULT_SLICE_COUNT, int layers = DEFAULT_RING_LAYERS) {
            WorldRenderer.ring(context, pos, colour, depth, radius, slices, layers);
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
}
