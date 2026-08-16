package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.Identifier;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.stb.STBImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.round;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.*;

/*
 * Original code (some functions) Copyright (c) 2026, odtheking (https://github.com/odtheking/OdinFabric/blob/main/src/main/kotlin/com/odtheking/odin/utils/ui/rendering/NVGRenderer.kt)
 * Modified/added functions by ricedotwho
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

@UtilityClass
@SuppressWarnings("unused")
public class NVGUtils implements Accessor {
    @Getter
    private final NVGPaint nvgPaint = NVGPaint.malloc();
    @Getter
    private final NVGColor nvgColor = NVGColor.malloc();
    @Getter
    private final NVGColor nvgColor2 = NVGColor.malloc();
    @Getter
    private final float[] fontBounds = new float[] {0f, 0f, 0f, 0f};

    @Getter
    private boolean drawing = false;

    private final Map<Image, NVGImage> images = new HashMap<>();


    private final Map<Font, NVGFont> fontMap = new HashMap<>();

    public final String ROBOTO = "Roboto Medium";
    public final String NUNITO = "Nunito";
    public final String SF_PRO = "SF Pro Rounded";
    public final String PRODUCT_SANS = "Product Sans";
    public final String JOSEFIN_BOLD = "JoseFin Bold";
    public final String JOSEFIN = "JoseFin";

    private final Map<String, Font> registeredFonts = new HashMap<>();

    private final Color TEXT_SHADOW = Color.fromHex(-16777216);

    @Getter
    private final long vg;

    static {
        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (vg == -1) {
            throw new ExceptionInInitializerError("[NVGUtils] Failed to init NanoVG");
        }
        registerFont(ROBOTO, Identifier.fromNamespaceAndPath("rsm", "font/roboto-medium.ttf"));
        registerFont(NUNITO, Identifier.fromNamespaceAndPath("rsm", "font/nunito.ttf"));
        registerFont(SF_PRO, Identifier.fromNamespaceAndPath("rsm", "font/sf-pro-rounded.ttf"));
        registerFont(PRODUCT_SANS, Identifier.fromNamespaceAndPath("rsm", "font/product-sans.ttf"));
        registerFont(JOSEFIN_BOLD, Identifier.fromNamespaceAndPath("rsm", "font/josefin-bold.ttf"));
        registerFont(JOSEFIN, Identifier.fromNamespaceAndPath("rsm", "font/josefin.ttf"));
    }

    public void registerFont(String name, Identifier path) {
        try {
            registeredFonts.put(name, new Font(name, mc.getResourceManager().getResource(path).get().open()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Font getFont(String name) {
        Font font = registeredFonts.get(name);
        if (font == null) {
            throw new IllegalArgumentException("Font \"" + name + "\" is not registered!");
        }
        return font;
    }

    public float devicePixelRatio() {
        try {
            Window window = mc.getWindow();
            int gw = window.getWidth();
            int sw = window.getScreenWidth();
            if (sw == 0) {
                return 1f;
            } else {
                return (float) gw / sw;
            }
        } catch (Exception e) {
            return 1f;
        }
    }

    public void beginFrame(float width, float height) {
        if (drawing) throw new IllegalStateException("[NVGUtils] NVG beginFrame called when already drawing");

        float dpr = devicePixelRatio();

        nvgBeginFrame(vg, width / dpr, height / dpr, dpr);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        drawing = true;
    }

    public void endFrame() {
        if (!drawing) throw new IllegalStateException("[NVGUtils] NVG endFrame called when not drawing");
        nvgEndFrame(vg);
        drawing = false;
    }

    public void push() {
        nvgSave(vg);
    }

    public void pop() {
        nvgRestore(vg);
    }

    public void scale(float x, float y) {
        nvgScale(vg, x, y);
    }

    public void scale(float factor) {
        nvgScale(vg, factor, factor);
    }

    public void translate(float x, float y) {
        nvgTranslate(vg, x, y);
    }

    public void rotate(float angle) {
        nvgRotate(vg, angle);
    }

    public void globalAlpha(float alpha) {
        nvgGlobalAlpha(vg, alpha);
    }

    public void pushScissor(float x, float y, float w, float h) {
        push();
        nvgIntersectScissor(vg, x, y, w, h);
    }

    public void popScissor() { pop(); }


    public void drawLine(float x, float y, float x1, float y1, float thickness, Color color) {
        nvgBeginPath(vg);
        nvgMoveTo(vg, x, y);
        nvgLineTo(vg, x1, y1);
        nvgStrokeWidth(vg, thickness);
        color(color);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }

    public void drawRect(double x, double y, double w, double h, double r, Color color) {
        drawRect((float) x, (float) y, (float) w, (float) h, (float) r, color);
    }

    public void drawRect(float x, float y, float w, float h, float r, Color color) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h + .5f, r);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawRect(float x, float y, float w, float h, Color color) {
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawRect(double x, double y, double w, double h, double r, int color) {
        drawRect((float) x, (float) y, (float) w, (float) h, (float) r, color);
    }

    public void drawRect(float x, float y, float w, float h, float r, int color) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h + .5f, r);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawRect(float x, float y, float w, float h, int color) {
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    private void drawRect2(float x, float y, float w, float h, float r, Color color) {
        nvgBeginPath(vg);

        nvgMoveTo(vg, x, y + h - r);
        nvgArcTo(vg, x, y + h - r, x + r, y + h, r);
        nvgLineTo(vg, x + w - r, y + h);
        nvgArcTo(vg, x + w - r, y + h, x + w, y + h - r, r);
        nvgLineTo(vg, x + w, y + r);
        nvgArcTo(vg, x + w, y, x + w - r, y, r);
        nvgLineTo(vg, x + r, y);
        nvgArcTo(vg, x, y, x, y + r, r);
        nvgLineTo(vg, x, y + h);

        nvgClosePath(vg);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawHalfRoundedRect(float x, float y, float w, float h, float r, boolean top, Color color) {
        nvgBeginPath(vg);

        if (top) {
            nvgMoveTo(vg, x, y + h);
            nvgLineTo(vg, x + w, y + h);
            nvgLineTo(vg, x + w, y + r);
            nvgArcTo(vg, x + w, y, x + w - r, y, r);
            nvgLineTo(vg, x + r, y);
            nvgArcTo(vg, x, y, x, y + r, r);
            nvgLineTo(vg, x, y + h);
        } else {
            nvgMoveTo(vg, x, y);
            nvgLineTo(vg, x + w, y);
            nvgLineTo(vg, x + w, y + h - r);
            nvgArcTo(vg, x + w, y + h, x + w - r, y + h, r);
            nvgLineTo(vg, x + r, y + h);
            nvgArcTo(vg, x, y + h, x, y + h - r, r);
            nvgLineTo(vg, x, y);
        }

        nvgClosePath(vg);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawOutlineRect(float x, float y, float w, float h, float r, float thickness, Color color) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, r);
        nvgStrokeWidth(vg, thickness);
        nvgPathWinding(vg, NVG_HOLE);
        color(color);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }

    public void drawOutlineRect(float x, float y, float w, float h, float thickness, Color color) {
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);
        nvgStrokeWidth(vg, thickness);
        nvgPathWinding(vg, NVG_HOLE);
        color(color);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }

    public void drawCircle(float x, float y, float r, Color color) {
        nvgBeginPath(vg);
        nvgCircle(vg, x, y, r);
        color(color);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawGradientRect(float x, float y, float w, float h, float r, Color from, Color to, Gradient direction) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, r);
        drawGradient(from, to, x, y, w, h, direction);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    public void drawGradientRect(float x, float y, float w, float h, float r, int from, int to, Gradient direction) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, r);
        drawGradient(from, to, x, y, w, h, direction);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    public void drawCircleOutline(float x, float y, float r, float thickness, Color color) {
        nvgBeginPath(vg);
        nvgCircle(vg, x, y, r);
        nvgStrokeWidth(vg, thickness);
        nvgPathWinding(vg, NVG_HOLE);
        color(color);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }


    private void drawGradient(int color1, int color2, float x, float y, float w, float h, Gradient direction) {
        color(color1, color2);
        switch (direction) {
            case LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint);
            case TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint);
        }
    }

    private void drawGradient(Color color1, Color color2, float x, float y, float w, float h, Gradient direction) {
        color(color1, color2);
        switch (direction) {
            case LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint);
            case TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint);
        }
    }

    public int createNVGImage(int texId, int width, int height) {
        return nvglCreateImageFromHandle(vg, texId, width, height, NVG_IMAGE_NEAREST | NVG_IMAGE_NODELETE);
    }

    public void renderImage(int image, int texWidth, int texHeight, float subX, float subY, float subW, float subH, float x, float y, float w, float h, float r) {
        if (image == -1) return;

        float sx = subX / texWidth;
        float sy = subY / texHeight;
        float sw = subW / texWidth;
        float sh = subH / texHeight;

        float iw = w / sw;
        float ih = h / sh;
        float ix = x - iw * sx;
        float iy = y - ih * sy;

        nvgImagePattern(vg, ix, iy, iw, ih, 0f, image, 1f, nvgPaint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h + 0.5f, r);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    public void renderImage(Image image, int texWidth, int texHeight, float subX, float subY, float subW, float subH, float x, float y, float w, float h, float r) {
        renderImage(getImage(image), texWidth, texHeight, subX, subY, subW, subH, x, y ,w, h, r);
    }

    public int getImage(Image image) {
        if (images.containsKey(image)) {
            return images.get(image).getNvg();
        } else {
            throw new IllegalStateException("Image (" + image.getIdentifier() + ") doesn't exist");
        }
    }

    public boolean hasImage(Image image) {
        return images.containsKey(image);
    }

    public void renderImage(Image image, float x, float y, float w, float h, float r, float alpha) {
        nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), alpha, nvgPaint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h + .5f, r);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    public void renderImage(Image image, float x, float y, float w, float h, float r) {
        renderImage(image, x, y, w, h, r, 1f);
    }

    public void renderImage(Image image, float x, float y, float w, float h) {
        renderImage(image, x, y, w, h, 0f, 1f);
    }

    public Image createImage(String path) {
        Optional<Image> opt = images.keySet().stream().filter(i -> Objects.equals(i.getIdentifier(), path)).findFirst();
        Image image = opt.orElseGet(() -> new Image(path));

        // todo: svg
        images.put(image, new NVGImage(0, loadImage(image)));
        image.loadDims();
        return image;
    }

    public Image createImage(String path, int flags) {
        Optional<Image> opt = images.keySet().stream().filter(i -> Objects.equals(i.getIdentifier(), path)).findFirst();
        Image image = opt.orElseGet(() -> new Image(path, flags));

        // todo: svg
        images.put(image, new NVGImage(0, loadImage(image)));
        image.loadDims();
        return image;
    }

    public Image createImage(String path, InputStream stream) {
        Optional<Image> opt = images.keySet().stream().filter(i -> Objects.equals(i.getIdentifier(), path)).findFirst();
        Image image = opt.orElseGet(() -> new Image(path, stream));

        // todo: svg
        images.put(image, new NVGImage(0, loadImage(image)));
        image.loadDims();
        return image;
    }

    public Image createImage(String id, ByteBuffer buffer) {
        Optional<Image> opt = images.keySet().stream().filter(i -> Objects.equals(i.getIdentifier(), id)).findFirst();
        Image image = opt.orElseGet(() -> new BufferImage(id, buffer));

        // todo: svg
        images.put(image, new NVGImage(0, loadImage(image)));
        image.loadDims();
        return image;
    }

    public Image createImage(String id, BufferedImage bufferedImage) {
        return createImage(id, bufferedImageToPNGByteBuffer(bufferedImage));
    }

    public static ByteBuffer bufferedImageToPNGByteBuffer(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// lowers reference count by 1, if it reaches 0 it gets deleted from mem
    public void deleteImage(Image image) {
        if (!images.containsKey(image)) return;
        NVGImage nvgImage = images.get(image);
        nvgImage.count--;
        if (nvgImage.count == 0) {
            nvgDeleteImage(vg, nvgImage.nvg);
            images.remove(image);
        }
    }

    public void deleteImageFully(Image image) {
        NVGImage nvgImage = images.remove(image);
        if (nvgImage == null) return;
        nvgDeleteImage(vg, nvgImage.nvg);
    }

    public static Pair<Integer, Integer> getImageSize(Image image) {
        NVGImage nvgImage = images.get(image);
        if (nvgImage == null) return new Pair<>(0, 0);
        int[] w = new int[1];
        int[] h = new int[1];

        NanoVG.nvgImageSize(vg, nvgImage.nvg, w, h);

        return new Pair<>(w[0], h[0]);
    }

    private int loadImage(Image image) {
        int[] w = new int[1];
        int[] h = new int[1];
        int[] channels = new int[1];
        ByteBuffer buffer = STBImage.stbi_load_from_memory(
                image.buffer(),
                w,
                h,
                channels,
                4
        );
        if (buffer == null) {
            throw new NullPointerException("Failed to load image: " + image.getIdentifier());
        } else {
            return nvgCreateImageRGBA(vg, w[0], h[0], image.getFlags(), buffer);
        }
    }

    public void drawCenteredText(String text, float x, float y, float size, Color color, Font font) {
        float halfWidth = getTextWidth(text, size, font) / 2f;
        drawText(text, x - halfWidth, y, size, color, font);
    }

    public void drawText(String text, float x, float y, float size, Color color, boolean shadow, Font font) {
        if (shadow) {
            drawTextShadow(text, x, y, size, color, font);
        } else {
            drawText(text, x, y, size, color, font);
        }
    }

    public void drawText(String text, float x, float y, float size, Color color, Font font) {
        nvgFontSize(vg, size);
        nvgFontFaceId(vg, getFontID(font));
        color(color);
        nvgBeginPath(vg);
        nvgFillColor(vg, nvgColor);
        nvgText(vg, x, y, text);
    }

    public void drawTextShadow(String text, float x, float y, float size, Color color, Font font) {
        nvgFontFaceId(vg, getFontID(font));
        nvgFontSize(vg, size);
        color(TEXT_SHADOW);
        nvgFillColor(vg, nvgColor);
        nvgText(vg, round(x + 1f), round(y + 1f), text);

        color(color);
        nvgFillColor(vg, nvgColor);
        nvgText(vg, round(x), round(y), text);
    }

    public float getTextWidth(String text, float size, Font font) {
        nvgFontSize(vg, size);
        nvgFontFaceId(vg, getFontID(font));
        return nvgTextBounds(vg, 0f, 0f, text, fontBounds);
    }

    public float getTextHeight(float size, Font font) {
        return getTextHeight("G", size, font);
    }

    public float getTextHeight(String content, float size, Font font) {
        nvgFontSize(vg, size);
        nvgFontFaceId(vg, getFontID(font));

        nvgTextBounds(vg, 0f, 0f, content, fontBounds);
        return fontBounds[3] - fontBounds[1]; // maxY - minY
    }

    public int getFontID(Font font) {
        if (fontMap.containsKey(font)) {
            return fontMap.get(font).id();
        } else {
            ByteBuffer buffer = font.buffer();
            int fontId = nvgCreateFontMem(vg, font.getName(), buffer, true);
            NVGFont f = new NVGFont(fontId, buffer);
            fontMap.put(font, f);
            return f.id();
        }
    }

    public void color(int color) {
        nvgRGBA(Color.getRedByte(color), Color.getGreenByte(color),Color.getBlueByte(color), Color.getAlphaByte(color), nvgColor);
    }

    public void color(Color color) {
        nvgRGBA(color.getRedByte(), color.getGreenByte(), color.getBlueByte(), color.getAlphaByte(), nvgColor);
    }

    public void color4f(float r, float g, float b, float a) {
        nvgRGBAf(r, g, b, a, nvgColor);
    }

    public void color(int color1, int color2) {
        nvgRGBA(Color.getRedByte(color1), Color.getGreenByte(color1),Color.getBlueByte(color1), Color.getAlphaByte(color1), nvgColor);
        nvgRGBA(Color.getRedByte(color2), Color.getGreenByte(color2),Color.getBlueByte(color2), Color.getAlphaByte(color2), nvgColor2);
    }

    public void color(Color color1, Color color2) {
        nvgRGBA(color1.getRedByte(), color1.getGreenByte(), color1.getBlueByte(), color1.getAlphaByte(), nvgColor);
        nvgRGBA(color2.getRedByte(), color2.getGreenByte(), color2.getBlueByte(), color2.getAlphaByte(), nvgColor2);
    }

    public boolean isHovering(double mouseX, double mouseY, float x, float y, float width, float height) {
        boolean isWithinX = mouseX >= x && mouseX <= x + width;
        boolean isWithinY = mouseY >= y && mouseY <= y + height;

        return isWithinX && isWithinY;
    }

    @Getter
    @AllArgsConstructor
    private class NVGImage {
        public int count;
        private int nvg;
    }
    private record NVGFont(int id, ByteBuffer buffer) {

    }
}
