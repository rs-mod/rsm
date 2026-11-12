package rsm.extensions.net.minecraft.client.gui.GuiGraphicsExtractor;

import com.ricedotwho.rsm.render.render2d.RenderState;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/Scissor.kt)
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
public class GuiGraphicsExtractorExtension {
    private static int skipStack = 0;

    public static void pushScissor(@This GuiGraphicsExtractor thiz, float x0, float y0, float x1, float y1) {
        var rect = new ScreenRectangle((int) x0, (int) y0, (int) x1, (int) y1).transformOutwardClamp(thiz.pose());
        if (rect == null) {
            skipStack++;
            return;
        }
        thiz.scissorStack.push(rect);
    }

    public static void popScissor(@This GuiGraphicsExtractor thiz) {
        if (skipStack > 0) {
            skipStack--;
            return;
        }
        thiz.disableScissor();
    }

    public static void push(@This GuiGraphicsExtractor thiz) {
        thiz.pose().pushMatrix();
        RenderState.push();
    }

    public static void pop(@This GuiGraphicsExtractor thiz) {
        thiz.pose().popMatrix();
        RenderState.pop();
    }
}
