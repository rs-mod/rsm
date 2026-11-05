package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lazily rasterized STB TrueType glyph atlas. One atlas exists per font/size pair.
 */
final class FontAtlas implements AutoCloseable {
    private static final int INITIAL_SIZE = 1024;
    private static final int PADDING = 1;

    private final Font font;
    private final int pixelSize;
    private final STBTTFontinfo info = STBTTFontinfo.create();
    private final float scale;
    private final float ascent;
    private final float descent;
    private final float lineHeight;

    private final Map<Integer, Glyph> glyphs = new HashMap<>();
    private final List<DynamicTexture> allTextures = new ArrayList<>();

    private NativeImage image;
    private DynamicTexture texture;
    private GpuSampler sampler;

    private int atlasWidth = INITIAL_SIZE;
    private int atlasHeight = INITIAL_SIZE;
    private int cursorX;
    private int cursorY;
    private int rowHeight;

    FontAtlas(Font font, int pixelSize) {
        this.font = font;
        this.pixelSize = Math.max(1, pixelSize);

        ByteBuffer buffer = font.buffer();
        if (!STBTruetype.stbtt_InitFont(info, buffer)) {
            throw new IllegalStateException("Failed to initialise font: " + font.getName());
        }

        this.scale = STBTruetype.stbtt_ScaleForPixelHeight(info, this.pixelSize);

        int[] ascentBuffer = new int[1];
        int[] descentBuffer = new int[1];
        int[] lineGapBuffer = new int[1];
        STBTruetype.stbtt_GetFontVMetrics(info, ascentBuffer, descentBuffer, lineGapBuffer);

        this.ascent = ascentBuffer[0] * scale;
        this.descent = descentBuffer[0] * scale;
        this.lineHeight = ascent - descent + lineGapBuffer[0] * scale;

        this.image = new NativeImage(atlasWidth, atlasHeight, true);
    }

    Font font() {
        return font;
    }

    int pixelSize() {
        return pixelSize;
    }

    float ascent() {
        return ascent;
    }

    float descent() {
        return descent;
    }

    float lineHeight() {
        return lineHeight;
    }

    TextMetrics measure(String text) {
        if (text == null || text.isEmpty()) {
            return new TextMetrics(0f, 0f, 0f, 0f, 0f, 0f);
        }

        float penX = 0f;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        boolean hasGlyph = false;
        int previous = -1;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint == '\n' || codePoint == '\r') {
                previous = -1;
                continue;
            }

            Glyph glyph = glyph(codePoint);
            if (previous != -1) {
                penX += kerning(previous, codePoint);
            }

            if (glyph.width > 0 && glyph.height > 0) {
                float gx = penX + glyph.xOffset;
                float gy = glyph.yOffset;
                minX = Math.min(minX, gx);
                minY = Math.min(minY, gy);
                maxX = Math.max(maxX, gx + glyph.width);
                maxY = Math.max(maxY, gy + glyph.height);
                hasGlyph = true;
            }

            penX += glyph.advance;
            previous = codePoint;
        }

        if (!hasGlyph) {
            return new TextMetrics(0f, 0f, 0f, 0f, 0f, 0f);
        }

        return new TextMetrics(maxX - minX, maxY - minY, minX, minY, maxX, maxY);
    }

    float measureWidth(String text) {
        return measure(text).width();
    }

    float measureHeight(String text) {
        return measure(text).height();
    }

    List<String> wrapLines(String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] paragraphs = text.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            if (maxWidth <= 0f || measure(paragraph).width() <= maxWidth) {
                lines.add(paragraph);
                continue;
            }

            String[] words = paragraph.split(" ", -1);
            StringBuilder current = new StringBuilder();

            for (String word : words) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (current.isEmpty() || measure(candidate).width() <= maxWidth) {
                    if (measure(word).width() > maxWidth && current.isEmpty()) {
                        hardBreak(word, maxWidth, lines);
                        current.setLength(0);
                    } else {
                        current.setLength(0);
                        current.append(candidate);
                    }
                    continue;
                }

                lines.add(current.toString());
                current.setLength(0);

                if (measure(word).width() > maxWidth) {
                    hardBreak(word, maxWidth, lines);
                } else {
                    current.append(word);
                }
            }

            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
        }

        return lines;
    }

    private void hardBreak(String word, float maxWidth, List<String> out) {
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < word.length(); ) {
            int codePoint = word.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String next = current + new String(Character.toChars(codePoint));
            if (!current.isEmpty() && measure(next).width() > maxWidth) {
                out.add(current.toString());
                current.setLength(0);
            }
            current.appendCodePoint(codePoint);
            i += charCount;
        }
        if (!current.isEmpty()) {
            out.add(current.toString());
        }
    }

    TextMetrics measureWrapped(String text, float maxWidth) {
        List<String> lines = wrapLines(text, maxWidth);
        if (lines.isEmpty()) {
            return new TextMetrics(0f, 0f, 0f, 0f, 0f, 0f);
        }

        float width = 0f;
        for (String line : lines) {
            width = Math.max(width, measure(line).width());
        }
        return new TextMetrics(width, lines.size() * lineHeight, 0f, 0f, width, lines.size() * lineHeight);
    }

    void prepare(String text) {
        if (text == null) return;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint == '\n' || codePoint == '\r') continue;
            glyph(codePoint);
        }
    }

    Glyph glyph(int codePoint) {
        Glyph glyph = glyphs.get(codePoint);
        if (glyph == null) {
            glyph = createGlyph(codePoint);
            glyphs.put(codePoint, glyph);
        }
        return glyph;
    }

    float kerning(int left, int right) {
        return STBTruetype.stbtt_GetCodepointKernAdvance(info, left, right) * scale;
    }

    private Glyph createGlyph(int codePoint) {
        int[] advanceWidth = new int[1];
        int[] leftSideBearing = new int[1];
        STBTruetype.stbtt_GetCodepointHMetrics(info, codePoint, advanceWidth, leftSideBearing);
        float advance = advanceWidth[0] * scale;

        int[] x0 = new int[1];
        int[] y0 = new int[1];
        int[] x1 = new int[1];
        int[] y1 = new int[1];
        STBTruetype.stbtt_GetCodepointBitmapBox(info, codePoint, scale, scale, x0, y0, x1, y1);

        int width = x1[0] - x0[0];
        int height = y1[0] - y0[0];
        if (width <= 0 || height <= 0) {
            return new Glyph(0, 0, 0, 0, x0[0], y0[0], advance);
        }

        int[] bitmapWidth = new int[1];
        int[] bitmapHeight = new int[1];
        int[] xOffset = new int[1];
        int[] yOffset = new int[1];
        ByteBuffer bitmap = STBTruetype.stbtt_GetCodepointBitmap(
                info, scale, scale, codePoint, bitmapWidth, bitmapHeight, xOffset, yOffset
        );

        if (bitmap == null || bitmapWidth[0] <= 0 || bitmapHeight[0] <= 0) {
            if (bitmap != null) STBTruetype.stbtt_FreeBitmap(bitmap);
            return new Glyph(0, 0, 0, 0, x0[0], y0[0], advance);
        }

        int glyphWidth = bitmapWidth[0];
        int glyphHeight = bitmapHeight[0];
        int widthWithPadding = glyphWidth + PADDING * 2;
        int heightWithPadding = glyphHeight + PADDING * 2;

        ensureSpace(widthWithPadding, heightWithPadding);

        // If the atlas is full and cannot grow (texture already exists), skip this glyph.
        if (cursorX + widthWithPadding > atlasWidth || cursorY + heightWithPadding > atlasHeight) {
            return new Glyph(0, 0, 0, 0, x0[0], y0[0], advance);
        }

        int atlasX = cursorX + PADDING;
        int atlasY = cursorY + PADDING;

        for (int y = 0; y < glyphHeight; y++) {
            int row = y * glyphWidth;
            for (int x = 0; x < glyphWidth; x++) {
                int alpha = bitmap.get(row + x) & 0xFF;
                if (alpha != 0) {
                    image.setPixel(atlasX + x, atlasY + y, (alpha << 24) | 0x00FFFFFF);
                }
            }
        }

        STBTruetype.stbtt_FreeBitmap(bitmap);

        cursorX += widthWithPadding;
        rowHeight = Math.max(rowHeight, heightWithPadding);

        return new Glyph(atlasX, atlasY, glyphWidth, glyphHeight, xOffset[0], yOffset[0], advance);
    }

    private void ensureSpace(int width, int height) {
        if (cursorX + width > atlasWidth) {
            cursorX = 0;
            cursorY += rowHeight;
            rowHeight = 0;
        }

        while (cursorY + height > atlasHeight || width > atlasWidth) {
            grow();
        }
    }

    private void grow() {
        int newWidth = atlasWidth * 2;
        int newHeight = atlasHeight * 2;

        NativeImage oldImage = image;
        NativeImage newImage = new NativeImage(newWidth, newHeight, true);
        if (oldImage != null) {
            oldImage.copyRect(newImage, 0, 0, 0, 0, atlasWidth, atlasHeight, false, false);
            // If no DynamicTexture owns oldImage yet, close it manually after copying.
            // If a DynamicTexture owns it, that texture stays alive (render states
            // reference it) and will close the image when disposed.
            if (texture == null) {
                oldImage.close();
            }
        }

        // Do NOT close the old texture — render states already flushed to guiRenderState
        // still reference its GpuTextureView through TextureSetup. Just drop our reference
        // so textureSetup() creates a fresh DynamicTexture for subsequent glyphs.
        texture = null;

        image = newImage;
        atlasWidth = newWidth;
        atlasHeight = newHeight;
    }

    TextureSetup textureSetup() {
        if (texture == null) {
            texture = new DynamicTexture(this::textureLabel, image);
            allTextures.add(texture);
        }
        if (sampler == null) {
            sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }
        return TextureSetup.singleTexture(texture.getTextureView(), sampler);
    }

    float u0(Glyph glyph) {
        return (float) glyph.atlasX / atlasWidth;
    }

    float v0(Glyph glyph) {
        return (float) glyph.atlasY / atlasHeight;
    }

    float u1(Glyph glyph) {
        return (float) (glyph.atlasX + glyph.width) / atlasWidth;
    }

    float v1(Glyph glyph) {
        return (float) (glyph.atlasY + glyph.height) / atlasHeight;
    }

    private String textureLabel() {
        return "rsm-font/" + font.getName() + "/" + pixelSize;
    }

    @Override
    public void close() {
        glyphs.clear();
        for (DynamicTexture t : allTextures) {
            t.close();
        }
        allTextures.clear();
        texture = null;
        sampler = null;
        if (image != null) {
            image.close();
            image = null;
        }
    }

    record Glyph(int atlasX, int atlasY, int width, int height, int xOffset, int yOffset, float advance) {
    }

    record TextMetrics(float width, float height, float minX, float minY, float maxX, float maxY) {
    }
}
