package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

import java.util.Arrays;

/**
 * A generic, batched GUI element. Vertices are already baked into GUI-scaled screen coordinates,
 * so the emitted pose is always identity.
 */
final class GuiElementState implements GuiElementRenderState {
    private final RenderPipeline pipeline;
    private final TextureSetup textureSetup;
    private final float[] positions;
    private final float[] uvs;
    private final int[] colors;
    private final int vertexCount;
    private final ScreenRectangle scissorArea;
    private final ScreenRectangle bounds;

    GuiElementState(RenderPipeline pipeline, TextureSetup textureSetup, float[] positions, float[] uvs,
                    int[] colors, int vertexCount, ScreenRectangle scissorArea, ScreenRectangle bounds) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.positions = positions;
        this.uvs = uvs;
        this.colors = colors;
        this.vertexCount = vertexCount;
        this.scissorArea = scissorArea;
        this.bounds = bounds;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        boolean textured = pipeline == RenderPipelines.GUI_TEXTURED;
        for (int i = 0; i < vertexCount; i++) {
            int argb = colors[i];
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            int a = (argb >>> 24) & 0xFF;
            VertexConsumer vertex = consumer.addVertexWith2DPose(
                    GuiRecorder.IDENTITY,
                    positions[i * 2],
                    positions[i * 2 + 1]
            );
            if (textured) {
                vertex.setUv(uvs[i * 2], uvs[i * 2 + 1]);
            }
            vertex.setColor(r, g, b, a);
        }
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        return bounds;
    }

    static GuiElementState copyOf(GuiElementState state) {
        return new GuiElementState(
                state.pipeline,
                state.textureSetup,
                Arrays.copyOf(state.positions, state.positions.length),
                state.uvs == null ? null : Arrays.copyOf(state.uvs, state.uvs.length),
                Arrays.copyOf(state.colors, state.colors.length),
                state.vertexCount,
                state.scissorArea,
                state.bounds
        );
    }
}
