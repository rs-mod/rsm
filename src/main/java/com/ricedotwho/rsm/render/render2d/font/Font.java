package com.ricedotwho.rsm.render.render2d.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.ricedotwho.rsm.render.CustomRenderPipelines;
import com.ricedotwho.rsm.render.render2d.RenderState;
import com.ricedotwho.rsm.render.render2d.font.msdf.Atlas;
import com.ricedotwho.rsm.render.render2d.font.msdf.Bounds;
import com.ricedotwho.rsm.render.render2d.font.msdf.Glyph;
import com.ricedotwho.rsm.render.render2d.font.msdf.MSDFInfo;
import com.ricedotwho.rsm.type.Color;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.TextColor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

class Font {
    private static final char FORMATTER = '§';
    private final AtlasTexture texture;
    private final MSDFInfo info;
    private final Int2ObjectOpenHashMap<CharData> charMap = new Int2ObjectOpenHashMap<>();

    private TextureSetup textureSetup = null;

    public Font(ByteBuffer buffer, MSDFInfo info) {
        this.info = info;
        this.texture = new AtlasTexture(NativeImage.read(buffer));
        info.glyphs().forEach((k, v) -> {
            if (v.atlasBounds() == null || v.planeBounds() == null) {
                charMap.put(k, new CharData(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, v.advance()));
            } else {
                charMap.put(k, new CharData(v, info.atlas()));
            }
        });
    }

    private TextureSetup textureSetup() {
        if (textureSetup == null) {
            textureSetup = TextureSetup.singleTexture(
                    texture.getTextureView(),
                    RenderSystem.getSamplerCache()
                            .getClampToEdge(FilterMode.LINEAR)
            );
        }

        return textureSetup;
    }

    private float scale(float size) {
        return size / this.info.metrics().emSize();
    }

    public float width(String test, float size) {
        var width = 0F;
        var chars = test.toCharArray();
        var scale = scale(size);

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == FORMATTER || i > 0 && chars[i - 1] == FORMATTER) continue;
            var data = charMap.get(c);
            if (data == null) data = charMap.get(32);
            width += data.xAdvance * scale;
        }

        return width;
    }

    public float height(float size) {
        return (info.metrics().ascender() - info.metrics().descender()) * scale(size);
    }

    public CharData get(int code) {
        return charMap.get(code);
    }

    public Vector2f measure(String text, float scale) {
        float width = 0f;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < text.length(); i++) {
            var glyph = get(text.charAt(i));

            if (glyph == null) continue;

            minY = Math.min(minY, glyph.y0);
            maxY = Math.max(maxY, glyph.y1);

            width += glyph.xAdvance;
        }

        float height = minY == Float.MAX_VALUE ? 0f : (maxY - minY) * scale;
        return new Vector2f(width * scale, height);
    }

    public Vector2f render(GuiGraphicsExtractor gfx, String text, float x, float y, float size, Color colour, Matrix3x2f pose, boolean shadow) {
        if (text.isEmpty()) return new Vector2f();
        var scale = scale(size);
        var dims = measure(text, scale);
        var alignment = RenderState.alignment();

        x -= alignment.calculateX(dims.x);
        y -= alignment.calculateY(dims.y);

        var bounds = new ScreenRectangle(
                (int) Math.floor(x),
                (int) Math.floor(y),
                (int) Math.ceil(dims.x),
                (int) Math.ceil(dims.y)
        ).transformOutward(pose);

        gfx.guiRenderState.addGuiElement(new Element(
                x, y, colour.getARGB(), pose,
                text, textureSetup(), gfx.scissorStack.peek(),
                bounds, shadow, this, scale
        ));

        return dims;
    }

    private record Element(float x, float y, int colour,
                           Matrix3x2fc pose, String content, TextureSetup textureSetup,
                           ScreenRectangle scissorArea, ScreenRectangle bounds,
                           boolean shadow, Font font, float scale
    ) implements GuiElementRenderState {

        @Override
        public void buildVertices(@NonNull VertexConsumer consumer) {
            TextColor format = null;
            var chars = content.toCharArray();

            var baseline = y + font.info.metrics().ascender() * scale;
            var cursor = x;

            for (int i = 0; i < chars.length; i++) {
                var c = chars[i];
                if (i > 0 && chars[i - 1] == FORMATTER) continue;

                // shadow is not allowed to be formatted
                if (c == FORMATTER) {
                    if (!shadow && i + 1 < chars.length) {
                        var cf = ChatFormatting.getByCode(chars[i + 1]);
                        format = cf == null ? null : TextColor.fromLegacyFormat(cf);
                    }
                    continue;
                }

                var glyph = font.get(c);
                if (glyph == null) glyph = font.get(32);

                var x0 = cursor + glyph.x0 * scale;
                var y0 = baseline + glyph.y0 * scale;
                var x1 = cursor + glyph.x1 * scale;
                var y1 = baseline + glyph.y1 * scale;

                if (x1 > x0 && y1 > y0) {
                    build(consumer,
                            x0, y0, x1, y1,
                            glyph.u0, glyph.v0,
                            glyph.u1, glyph.v1,
                            format == null ? colour : format.getValue() | 0xFF000000
                    );
                }

                cursor += glyph.xAdvance * scale;
            }
        }

        private void build(VertexConsumer consumer, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int c) {
            vertex(consumer, x0, y0, u0, v0, c);
            vertex(consumer, x0, y1, u0, v1, c);
            vertex(consumer, x1, y1, u1, v1, c);
            vertex(consumer, x1, y0, u1, v0, c);
        }

        private void vertex(VertexConsumer consumer, float x, float y, float u, float v, int c) {
            consumer.addVertexWith2DPose(pose, x, y).setColor(c).setUv(u, v);
        }

        @Override
        public @NonNull RenderPipeline pipeline() {
            return CustomRenderPipelines.UI_TEXT;
        }

        @Override
        public @NonNull TextureSetup textureSetup() {
            return textureSetup;
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return scissorArea;
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            return bounds;
        }
    }

    public record CharData(float x0, float y0,
                           float x1, float y1,
                           float u0, float v0,
                           float u1, float v1,
                           float xAdvance) {
        public CharData(Glyph glyph, Atlas atlas) {
            Bounds plane = glyph.planeBounds();
            Bounds bounds = glyph.atlasBounds();

            if (plane == null || bounds == null) {
                throw new IllegalArgumentException("Cannot create CharData with null bounds!");
            }

            float x0 = plane.left();
            float y0 = -plane.top();

            float x1 = plane.right();
            float y1 = -plane.bottom();

            float u0 = bounds.left() / atlas.width();
            float v0 = 1.0f - bounds.top() / atlas.height();

            float u1 = bounds.right() / atlas.width();
            float v1 = 1.0f - bounds.bottom() / atlas.height();

             this(
                    x0, y0, x1, y1,
                    u0, v0, u1, v1,
                    glyph.advance()
            );
        }
    }

    public void close() {
        this.texture.close();
    }
}
