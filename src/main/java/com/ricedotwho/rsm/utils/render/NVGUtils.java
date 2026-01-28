package com.ricedotwho.rsm.utils.render;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.*;

import org.lwjgl.nanovg.NanoSVG.*;
import org.lwjgl.nanovg.NanoVG.*;
import org.lwjgl.nanovg.NanoVGGL3.*;

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
public class NVGUtils implements Accessor {
    @Getter
    private final NVGPaint nvgPaint = NVGPaint.malloc();
    @Getter
    private final NVGColor nvgColor = NVGColor.malloc();
    @Getter
    private final NVGColor nvgColor2 = NVGColor.malloc();

    private Scissor scissor = null;
    private boolean drawing = false;

    public final Font ROBOTO;
    public final Font NUNITO;
    public final Font SF_PRO;
    public final Font PRODUCT_SANS;
    public final Font JOSEFIN_BOLD;
    public final Font JOSEFIN;

    private final float[] fontBounds = new float[] {0f, 0f, 0f, 0f};

    private final Colour TEXT_SHADOW = new Colour(-16777216);

    static {
        try {
            ROBOTO = new Font("Roboto Medium", mc.getResourceManager().getResource(ResourceLocation.parse("rsm:font/roboto-medium.ttf")).get().open());
            NUNITO = new Font("Nunito", mc.getResourceManager().getResource(ResourceLocation.parse("rsm:font/nunito.ttf")).get().open());
            SF_PRO = new Font("SF Pro Rounded", mc.getResourceManager().getResource(ResourceLocation.parse("rsm:font/sf-pro-rounded.ttf")).get().open());
            PRODUCT_SANS = new Font("Product Sans", mc.getResourceManager().getResource(ResourceLocation.parse("rsm:font/product-sans.ttf")).get().open());
            JOSEFIN_BOLD = new Font("JoseFin Bold", mc.getResourceManager().getResource(ResourceLocation.parse("rsm:font/josefin-bold.ttf")).get().open());
            JOSEFIN = new Font("JoseFin", NVGUtils.class.getResourceAsStream("/assets/rsm/font/josefin.ttf"));//mc.getResourceManager().getResource(ResourceLocation.parse("rsm:font/josefin.ttf")).get().open());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<Image, NVGImage> images = new HashMap<>();
    private final Map<Font, NVGFont> fontMap = new HashMap<>();

    @Getter
    private long vg = -1;

    static {
        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (vg == -1) {
            throw new ExceptionInInitializerError("[NVGUtils] Failed to init NanoVG");
        }
    }

    public float devicePixelRatio() {
        try {
            Window window = mc.getWindow();
            int gw = window.getWidth();
            int sw = window.getScreenWidth();
            if (sw == 0) {
                return 1f;
            } else {
                return (float) gw;
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
        scissor = new Scissor(scissor, x, y, w + x, h + y);
        scissor.applyScissor();
    }

    public void popScissor() {
        nvgResetScissor(vg);
        if (scissor != null) {
            scissor = scissor.previous;
            if (scissor != null) scissor.applyScissor();
        }
    }

    public void drawLine(float x, float y, float x1, float y1, float thickness, Colour colour) {
        nvgBeginPath(vg);
        nvgMoveTo(vg, x, y);
        nvgLineTo(vg, x1, y1);
        nvgStrokeWidth(vg, thickness);
        colour(colour);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }

    public void drawLine(Vector2d from, Vector2f to, float thickness, Colour colour) {
        drawLine((float) from.x, (float) from.y, to.x, to.y, thickness, colour);
    }

    public void drawRect(double x, double y, double w, double h, double r, Colour colour) {
        drawRect((float) x, (float) y, (float) w, (float) h, (float) r, colour);
    }


    public void drawRect(float x, float y, float w, float h, float r, Colour colour) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h + .5f, r);
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawRect(float x, float y, float w, float h, Colour colour) {
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    private void drawRect2(float x, float y, float w, float h, float r, Colour colour) {
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
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawHalfRoundedRect(float x, float y, float w, float h, float r, boolean top, Colour colour) {
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
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawOutlineRect(float x, float y, float w, float h, float r, float thickness, Colour colour) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, r);
        nvgStrokeWidth(vg, thickness);
        nvgPathWinding(vg, NVG_HOLE);
        colour(colour);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }

    public void drawOutlineRect(float x, float y, float w, float h, float thickness, Colour colour) {
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);
        nvgStrokeWidth(vg, thickness);
        nvgPathWinding(vg, NVG_HOLE);
        colour(colour);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
    }

    public void drawCircle(float x, float y, float r, Colour colour) {
        nvgBeginPath(vg);
        nvgCircle(vg, x, y, r);
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public void drawGradientRect(float x, float y, float w, float h, float r, Colour from, Colour to, Gradient direction) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, r);
        drawGradient(from, to, x, y, w, h, direction);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    private void drawGradient(Colour color1, Colour color2, float x, float y, float w, float h, Gradient direction) {
        colour(color1, color2);
        switch (direction) {
            case LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint);
            case TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint);
        }
    }

    public void drawDropShadow(float x, float y, float w, float h, float blur, float spread, float r) {
        nvgRGBA((byte) 0, (byte) 0, (byte) 0, (byte) 125, nvgColor);
        nvgRGBA((byte) 0, (byte) 0, (byte) 0, (byte) 0, nvgColor2);

        nvgBoxGradient(
                vg,
                x - spread,
                y - spread,
                w + 2 * spread,
                h + 2 * spread,
                r + spread,
                blur,
                nvgColor,
                nvgColor2,
                nvgPaint
        );
        nvgBeginPath(vg);
        nvgRoundedRect(
                vg,
                x - spread - blur,
                y - spread - blur,
                w + 2 * spread + 2 * blur,
                h + 2 * spread + 2 * blur,
                r + spread
        );
        nvgRoundedRect(vg, x, y, w, h, r);
        nvgPathWinding(vg, NVG_HOLE);
        nvgFillPaint(vg, nvgPaint);
    }

    public void drawArrow(float x, float y, float size, float width, Colour colour, boolean expanded) {
        push();
        translate(x, y);
        scale(size, size);

        nvgBeginPath(vg);

        if (expanded) {
            nvgMoveTo(vg, 0.2f, 0.3f);
            nvgLineTo(vg, 0.5f, 0.7f);

            nvgMoveTo(vg, 0.5f, 0.7f);
            nvgLineTo(vg, 0.8f, 0.3f);
        } else {
            nvgMoveTo(vg, 0.2f, 0.7f);
            nvgLineTo(vg, 0.5f, 0.3f);

            nvgMoveTo(vg, 0.5f, 0.3f);
            nvgLineTo(vg, 0.8f, 0.7f);
        }

        nvgStrokeWidth(vg, width);
        colour(colour);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
        pop();
        nvgColor.free();
    }

    public void drawCheckmark(float x, float y, float size, float width, Colour colour) {
        push();
        translate(x, y);
        scale(size, size);

        nvgBeginPath(vg);

        nvgMoveTo(vg, 0.1f, 0.7f);
        nvgLineTo(vg, 0.3f, 0.9f);

        nvgMoveTo(vg, 0.3f, 0.9f);
        nvgLineTo(vg, 0.8f, 0.4f);

        nvgStrokeWidth(vg, width);
        colour(colour);
        nvgStrokeColor(vg, nvgColor);
        nvgStroke(vg);
        pop();
        nvgColor.free();
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

    private int getImage(Image image) {
        if (images.containsKey(image)) {
            return images.get(image).getNvg();
        } else {
            throw new IllegalStateException("Image (" + image.getIdentifier() + ") doesn't exist");
        }
    }

    public void renderImage(Image image, float x, float y, float w, float h, float r) {
        nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h + .5f, r);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    public void renderImage(Image image, float x, float y, float w, float h) {
        nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint);
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h + .5f);
        nvgFillPaint(vg, nvgPaint);
        nvgFill(vg);
    }

    public Image createImage(String path) {
        Optional<Image> opt = images.keySet().stream().filter(i -> Objects.equals(i.getIdentifier(), path)).findFirst();
        Image image = opt.orElseGet(() -> new Image(path));

        // todo: svg
        images.put(image, new NVGImage(0, loadImage(image)));
        return image;
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
            return nvgCreateImageRGBA(vg, w[0], h[0], 0, buffer);
        }
    }

    // Text

    private int getFontID(Font font) {
        if (fontMap.containsKey(font)) {
            return fontMap.get(font).id();
        } else {
            ByteBuffer buffer = font.buffer();
            NVGFont f = new NVGFont(nvgCreateFontMem(vg, font.getName(), buffer, false), buffer);
            fontMap.put(font, f);
            return f.id();
        }
    }

    public void drawText(String content, float x, float y, float size, Colour colour, Font font) {
        nvgFontSize(vg, size);
        nvgFontFaceId(vg, getFontID(font));
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgText(vg, x, y + .5f, content);
    }

    public void drawTextShadow(String content, float x, float y, float size, Colour colour, Font font) {
        nvgFontFaceId(vg, getFontID(font));
        nvgFontSize(vg, size);
        colour(TEXT_SHADOW);
        nvgFillColor(vg, nvgColor);
        nvgText(vg, Math.round(x + 2f), Math.round(y + 2f), content);

        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgText(vg, Math.round(x), Math.round(y), content);
    }

    public float getTextWidth(String content, float size, Font font) {
        nvgFontSize(vg, size);
        int fontID = getFontID(font);
        nvgFontFaceId(vg, fontID);
        return nvgTextBounds(vg, 0f, 0f, content, fontBounds);
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

    public void drawWrappedText(String content, float x, float y, float w, float size, Colour colour, Font font) {
        drawWrappedText(content, x, y, w, size, colour, font, 1f);
    }

    public void drawWrappedText(String content, float x, float y, float w, float size, Colour colour, Font font, float lineHeight) {
        nvgFontSize(vg, size);
        nvgFontFaceId(vg, getFontID(font));
        nvgTextLineHeight(vg, lineHeight);
        colour(colour);
        nvgFillColor(vg, nvgColor);
        nvgTextBox(vg, x, y, w, content);
    }

    public float[] drawWrappedTextBounds(String content, float w, float size, Font font) {
        return drawWrappedTextBounds(content, w, size, font, 1f);
    }

    public float[] drawWrappedTextBounds(String content, float w, float size, Font font, float lineHeight) {
        float[] bounds = new float[4];
        nvgFontSize(vg, size);
        nvgFontFaceId(vg, getFontID(font));
        nvgTextLineHeight(vg, lineHeight);
        nvgTextBoxBounds(vg, 0f, 0f, w, content, bounds);
        return bounds; // [minX, minY, maxX, maxY]
    }

    public void colour(Colour colour) {
        nvgRGBA(colour.getRedByte(), colour.getGreenByte(), colour.getBlueByte(), colour.getAlphaByte(), nvgColor);
    }

    public void colour(Colour colour1, Colour colour2) {
        nvgRGBA(colour1.getRedByte(), colour1.getGreenByte(), colour1.getBlueByte(), colour1.getAlphaByte(), nvgColor);
        nvgRGBA(colour2.getRedByte(), colour2.getGreenByte(), colour2.getBlueByte(), colour2.getAlphaByte(), nvgColor);
    }

    public boolean isHovering(double mouseX, double mouseY, float x, float y, float width, float height) {
        boolean isWithinX = mouseX >= x && mouseX <= x + width;
        boolean isWithinY = mouseY >= y && mouseY <= y + height;

        return isWithinX && isWithinY;
    }

    @Getter
    @AllArgsConstructor
    private class Scissor {
        private Scissor previous;
        private float x;
        private float y;
        private float maxX;
        private float maxY;
        private void applyScissor() {
            if (previous == null) nvgScissor(vg, x, y, maxX - x, maxY - y);
            else {
                float x = Math.max(this.x, previous.x);
                float y = Math.max(this.y, previous.y);
                float width = Math.max(0f, (Math.min(maxX, previous.maxX) - x));
                float height = Math.max(0f, (Math.min(maxY, previous.maxY) - y));
                nvgScissor(vg, x, y, width, height);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    private class NVGImage {
        public int count;
        private int nvg;
    }
    private record NVGFont(int id, ByteBuffer buffer) { }
}
