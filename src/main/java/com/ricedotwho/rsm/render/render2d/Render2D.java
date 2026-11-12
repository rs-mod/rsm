package com.ricedotwho.rsm.render.render2d;

import com.ricedotwho.rsm.type.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import static com.ricedotwho.rsm.render.render2d.RenderState.argb;

/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/RoundedRect.kt)
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

public class Render2D {
    /**
     * Global alpha bs <br>
     * reset using {@link GuiGraphicsExtractor#pop} and {@link GuiGraphicsExtractor#push} <br>
     * or calling this with {@link Float#NaN}
     */
    public static void globalAlpha(Float alpha) {
        RenderState.globalAlpha(alpha);
    }

    public static void rect(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color colour, Corners corners = Corners.NONE) {
        var argb = argb(colour);
        RoundedRectRenderer.submit(gfx, x0, y0, x0 + x1, y0 + y1, argb, argb, argb, argb, corners, 0F);
    }

    public static void rect(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color colour, float radius) {
        rect(gfx, x0, y0, x1, y1, colour, new Corners(radius));
    }

    public static void rect2(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color colour, float radius = 0F) {
        rect(gfx, x0, y0, x1, y1, colour, new Corners(radius));
    }

    public static void rectClipped(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, int clipX0, int clipY0, int clipX1, int clipY1, Color colour, Corners corners = Corners.NONE) {
        var argb = argb(colour);
        RoundedRectRenderer.submit(gfx, x0, y0, x0 + x1, y0 + y1, argb, argb, argb, argb, corners, 0F, clip:new ScreenRectangle(clipX0, clipY0, clipX1,clipY1));
    }

    public static void shadow(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color colour, float blur, Corners corners = Corners.NONE) {
        var argb = argb(colour);
        RoundedRectRenderer.submitShadow(gfx, x0, y0, x0 + x1, y0 + y1, argb, corners, blur);
    }

    public static void outline(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color colour, float width, Corners corners = Corners.NONE) {
        if (width > 0F) {
            var argb = argb(colour);
            RoundedRectRenderer.submit(gfx, x0, y0, x0 + x1, y0 + y1, argb, argb, argb, argb, corners, width);
        }
    }

    public static void outline(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color colour, float width, float radius) {
        if (width > 0F) {
            var argb = argb(colour);
            RoundedRectRenderer.submit(gfx, x0, y0, x0 + x1, y0 + y1, argb, argb, argb, argb, new Corners(radius), width);
        }
    }

    public static void rectOutline(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color outline, Color fill, float width = 1F, Corners corners = Corners.NONE) {
        if (width > 0F) {
            var argb = argb(fill);
            RoundedRectRenderer.submit(gfx, x0, y0, x0 + x1, y0 + y1, argb, argb, argb, argb, corners, -width);
            outline(gfx, x0, y0, x1, y1, outline, width, corners);
        } else {
            rect(gfx, x0, y0, x1, y1, fill, corners);
        }
    }

    public static void rectOutline(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color outline, Color fill, float radius, float width) {
        rectOutline(gfx, x0, y0, x1, y1, outline, fill, width, new Corners(radius));
    }

    public static void gradient(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Color startColour, Color endColour, Gradient direction = Gradient.LeftToRight, Corners corners = Corners.NONE) {
        var start = argb(startColour);
        var end = argb(endColour);
        var middle = midpoint(start, end);
        var p = switch (direction) {
            case Gradient.LeftToRight -> new int[]{start, end, start, end};
            case Gradient.TopToBottom -> new int[]{start, start, end, end};
            case Gradient.TopLeftToBottomRight -> new int[]{start, middle, middle, end};
            case Gradient.BottomLeftToTopRight -> new int[]{middle, end, start, middle};
        };
        RoundedRectRenderer.submit(gfx, x0, y0, x0 + x1, y0 + y1, p[0], p[1], p[2], p[3], corners, 0f);
    }

    public static void circle(GuiGraphicsExtractor gfx, float centerX, float centerY, float radius, Color colour) {
        var argb = argb(colour);
        RoundedRectRenderer.submit(
                gfx, centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                argb, argb, argb, argb, new Corners(radius), 0f
        );
    }

    public static void circleOutline(GuiGraphicsExtractor gfx, float centerX, float centerY, float radius, float width, Color colour) {
        var argb = argb(colour);
        RoundedRectRenderer.submit(
                gfx, centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                argb, argb, argb, argb, new Corners(radius), width
        );
    }

    public static void texture(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Identifier texture, Color tint = Color.WHITE, Corners corners = Corners.NONE) {
        var argb = argb(tint);
        RoundedRectRenderer.submit(gfx, x0, y0, x1, y1, argb, argb, argb, argb, corners, 0f, texture);
    }

    public static void image(GuiGraphicsExtractor gfx, float x0, float y0, float x1, float y1, Image texture, Color tint = Color.WHITE, Corners corners = Corners.NONE) {
        var argb = argb(tint);
        RoundedRectRenderer.submitTexture(gfx, x0, y0, x0 + x1, y0 + y1, argb, argb, argb, argb, corners, 0f, texture);
    }

    public static void image(GuiGraphicsExtractor gfx, float x, float y, Image texture, Color tint = Color.WHITE, Corners corners = Corners.NONE) {
        var argb = argb(tint);
        RoundedRectRenderer.submitTexture(gfx, x, y, x + texture.width(), y + texture.height(), argb, argb, argb, argb, corners, 0f, texture);
    }

    public static void line(GuiGraphicsExtractor gfx, float x1, float y1, float x2, float y2, Color colour, float width = 1F) {
        var dx = x2 - x1;
        var dy = y2 - y1;
        var length = Math.hypot(dx, dy);

        gfx.pose().pushMatrix();
        gfx.pose().translate(x1, y1);
        gfx.pose().mul(new Matrix3x2f().identity().rotate((float) Math.atan2(dy, dx)));
        gfx.pose().scale(1f, width);

        gfx.fill(0, -1, (int) Math.ceil(length), 1, argb(colour));

        gfx.pose().popMatrix();
    }

    private static int midpoint(int a, int b) {
        return (mix(a >>> 24 & 0xFF, b >>> 24 & 0xFF) << 24)
                | (mix(a >>> 16 & 0xFF, b >>> 16 & 0xFF) << 16)
                | (mix(a >>> 8 & 0xFF, b >>> 8 & 0xFF) << 8)
                | mix(a & 0xFF, b & 0xFF);
    }

    private static int mix(int a, int b) {
        return (a + b) / 2;
    }
}
