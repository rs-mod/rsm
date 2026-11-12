package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.ricedotwho.rsm.render.CustomRenderPipelines;
import com.ricedotwho.rsm.type.Accessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.NonNull;

/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/RoundedRectRenderer.kt)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

public class RoundedRectRenderer implements Accessor {
    private static final float AA_PADDING = 2f;
    private static final float HALF_EXTENT_SCALE = 8f;
    private static final float RADIUS_SCALE = 4f;
    private static final float MAX_RADIUS = 255f / RADIUS_SCALE;
    private static final int ALPHA_MASK = 0xFF << 24;
    private static final TextureSetup NO_TEXTURE = TextureSetup.noTexture();

    public static final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
            .addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
            .addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
            .addAttribute(DefaultVertexFormat.UV1_SEMANTIC_NAME, GpuFormat.RG16_SINT)
            .addAttribute(DefaultVertexFormat.UV2_SEMANTIC_NAME, GpuFormat.RG16_SINT)
            .addAttribute(DefaultVertexFormat.LINE_WIDTH_SEMANTIC_NAME, GpuFormat.R32_FLOAT)
            .build();

    static void submit(GuiGraphicsExtractor gfx,
                       float x0, float y0, float x1, float y1,
                       int topLeftC, int topRightC, int bottomLeftC, int bottomRightC,
                       Corners corners,
                       float width,
                       Identifier texture = null,
                       ScreenRectangle clip = null) {

        emit(gfx, x0, y0, x1, y1,
                topLeftC, topRightC, bottomRightC, bottomLeftC,
                corners, width, AA_PADDING,
                texture != null ? CustomRenderPipelines.ROUNDED_RECT_TEXTURED : CustomRenderPipelines.ROUNDED_RECT,
                textureSetup(texture), clip
        );
    }

    static void submitTexture(GuiGraphicsExtractor gfx,
                       float x0, float y0, float x1, float y1,
                       int topLeftC, int topRightC, int bottomLeftC, int bottomRightC,
                       Corners corners,
                       float width,
                       @NotNull Image texture,
                       ScreenRectangle clip = null) {

        emit(gfx, x0, y0, x1, y1,
                topLeftC, topRightC, bottomRightC, bottomLeftC,
                corners, width, AA_PADDING,
                CustomRenderPipelines.ROUNDED_RECT_TEXTURED,
                texture.textureSetup(), clip
        );
    }

    static void submitShadow(GuiGraphicsExtractor gfx,
                       float x0, float y0, float x1, float y1,
                       int colour,
                       Corners corners,
                       float blur) {

        emit(gfx, x0, y0, x1, y1, colour, colour, colour, colour,
                corners, blur, AA_PADDING + blur,
                CustomRenderPipelines.ROUNDED_RECT_SHADOW, TextureSetup.noTexture(), null
        );
    }

    private static void emit(GuiGraphicsExtractor context,
                      float x0, float y0, float x1, float y1,
                      int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor,
                      Corners corners, float edgeWidth, float padding,
                      RenderPipeline pipeline, TextureSetup textureSetup,
                      @Nullable ScreenRectangle clip) {
        if (((topLeftColor | topRightColor | bottomRightColor | bottomLeftColor) & ALPHA_MASK) == 0) return;

        var left = Math.min(x0, x1);
        var top = Math.min(y0, y1);
        var right = Math.max(x0, x1);
        var bottom = Math.max(y0, y1);

        var halfWidth = (right - left) * 0.5f;
        var halfHeight = (bottom - top) * 0.5f;
        if (halfWidth <= 0f || halfHeight <= 0f) return;

        var pose = RenderState.snapshotPose(context.pose());
        var scissor = context.scissorStack.peek();

        var quadX0 = (int) Math.max(Math.floor(left - padding), clip == null ? Integer.MIN_VALUE : clip.left());
        var quadY0 = (int) Math.max(Math.floor(top - padding), clip == null ? Integer.MIN_VALUE : clip.top());
        var quadX1 = (int) Math.min(Math.ceil(right + padding), clip == null ? Integer.MAX_VALUE : clip.right());
        var quadY1 = (int) Math.min(Math.ceil(bottom + padding), clip == null ? Integer.MAX_VALUE : clip.bottom());
        if (quadX0 >= quadX1 || quadY0 >= quadY1) return;

        var quad = new ScreenRectangle(quadX0, quadY0, quadX1 - quadX0, quadY1 - quadY0).transformOutward(pose);
        var bounds = scissor != null ? scissor.intersection(quad) : quad;

        context.guiRenderState.addGuiElement(
                new Element(
                        pose,
                        (float) quadX0, (float) quadY0, (float) quadX1, (float) quadY1,
                        (left + right) * 0.5f, (top + bottom) * 0.5f,
                        topLeftColor, topRightColor, bottomRightColor, bottomLeftColor,
                        fixedPoint(halfWidth), fixedPoint(halfHeight),
                        packRadius(corners.topLeft()) | (packRadius(corners.topRight()) << 8),
                        packRadius(corners.bottomRight()) | (packRadius(corners.bottomLeft()) << 8),
                        edgeWidth, pipeline, textureSetup,
                        scissor, bounds
                )
        );
    }

    private static TextureSetup textureSetup(Identifier texture) {
        if (texture == null) return NO_TEXTURE;
        var view = mc.getTextureManager().getTexture(texture).getTextureView();
        return TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
    }

    private static int fixedPoint(float value) {
        return Math.clamp(Math.round(value * HALF_EXTENT_SCALE), Short.MIN_VALUE, Short.MAX_VALUE);
    }

    private static int packRadius(float radius) {
        return Math.clamp(Math.round(Math.clamp(radius, 0f, MAX_RADIUS) * RADIUS_SCALE), 0, 255);
    }

    private record Element(Matrix3x2fc pose, float x0, float y0, float x1, float y1, float centerX, float centerY,
                           int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor,
                           int packedHalfWidth, int packedHalfHeight, int packedRadiiTop, int packedRadiiBottom,
                           float edgeWidth, RenderPipeline pipeline, TextureSetup textureSetup,
                           ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {
        @Override
        public void buildVertices(@NonNull VertexConsumer vertexConsumer) {
            v(vertexConsumer, x0, y0, topLeftColor);
            v(vertexConsumer, x0, y1, bottomLeftColor);
            v(vertexConsumer, x1, y1, bottomRightColor);
            v(vertexConsumer, x1, y0, topRightColor);
        }

        private void v(@NonNull VertexConsumer vertexConsumer, float x, float y, int c) {
            vertexConsumer.addVertexWith2DPose(pose, x, y)
                    .setColor(c)
                    .setUv(x - centerX, y - centerY)
                    .setUv1(packedHalfWidth, packedHalfHeight)
                    .setUv2(packedRadiiTop, packedRadiiBottom)
                    .setLineWidth(edgeWidth);
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return this.scissorArea;
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            return this.bounds;
        }
    }
}
