package rsm.extensions.net.minecraft.client.gui.navigation.ScreenRectangle;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import static com.ricedotwho.rsm.type.Accessor.mc;

/*
 * Modified, original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/Scissor.kt)
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

@Extension
public class ScreenRectangleExtension {
    public static ScreenRectangle transformOutward(@This ScreenRectangle thiz, Matrix3x2fc pose) {
        var corner = new Vector2f();
        var l = (float) thiz.left();
        var t = (float) thiz.top();
        var r = (float) thiz.right();
        var b = (float) thiz.bottom();

        pose.transformPosition(l, t, corner);
        var minX = corner.x;
        var maxX = corner.x;
        var minY = corner.y;
        var maxY = corner.y;

        pose.transformPosition(r, t, corner);
        minX = Math.min(minX, corner.x); maxX = Math.max(maxX, corner.x);
        minY = Math.min(minY, corner.y); maxY = Math.max(maxY, corner.y);

        pose.transformPosition(r, b, corner);
        minX = Math.min(minX, corner.x); maxX = Math.max(maxX, corner.x);
        minY = Math.min(minY, corner.y); maxY = Math.max(maxY, corner.y);

        pose.transformPosition(l, b, corner);
        minX = Math.min(minX, corner.x); maxX = Math.max(maxX, corner.x);
        minY = Math.min(minY, corner.y); maxY = Math.max(maxY, corner.y);

        var left = (int) Math.floor(minX);
        var top = (int) Math.floor(minY);
        return new ScreenRectangle(left, top, (int) Math.ceil(maxX) - left, (int) Math.ceil(maxY) - top);
    }

    public static @Nullable ScreenRectangle transformOutwardClamp(@This ScreenRectangle thiz, Matrix3x2fc pose) {
        var corner = new Vector2f();
        var l = (float) thiz.left();
        var t = (float) thiz.top();
        var r = (float) thiz.right();
        var b = (float) thiz.bottom();

        pose.transformPosition(l, t, corner);
        var minX = corner.x;
        var maxX = corner.x;
        var minY = corner.y;
        var maxY = corner.y;

        pose.transformPosition(r, t, corner);
        minX = Math.min(minX, corner.x); maxX = Math.max(maxX, corner.x);
        minY = Math.min(minY, corner.y); maxY = Math.max(maxY, corner.y);

        pose.transformPosition(r, b, corner);
        minX = Math.min(minX, corner.x); maxX = Math.max(maxX, corner.x);
        minY = Math.min(minY, corner.y); maxY = Math.max(maxY, corner.y);

        pose.transformPosition(l, b, corner);
        minX = Math.min(minX, corner.x); maxX = Math.max(maxX, corner.x);
        minY = Math.min(minY, corner.y); maxY = Math.max(maxY, corner.y);

        var left = (int) Math.floor(minX);
        var top = (int) Math.floor(minY);
        return clamp(left, top, (int) Math.ceil(maxX), (int) Math.ceil(maxY));
    }

    private static ScreenRectangle clamp(int l, int t, int r, int b) {
        var window = mc.getWindow();
        var scale = window.getGuiScale();
        int left = Mth.clamp(l * scale, 0, window.getWidth()) / scale;
        int top = Mth.clamp(t * scale, 0, window.getHeight()) / scale;
        int right = Mth.clamp(r * scale, 0, window.getWidth()) / scale;
        int bottom = Mth.clamp(b * scale, 0, window.getHeight()) / scale;

        var width = right - left;
        var height = bottom - top;
        if (width <= 0 || height <= 0) return null;
        return new ScreenRectangle(left, top, width, height);
    }
}
