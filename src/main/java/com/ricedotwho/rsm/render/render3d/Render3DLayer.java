package com.ricedotwho.rsm.render.render3d;

import com.ricedotwho.rsm.render.CustomRenderPipelines;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/CustomRenderType.kt)
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
public final class  Render3DLayer {
    public final RenderType LINES_ESP = RenderType.create(
            "lines-esp",
            RenderSetup.builder(CustomRenderPipelines.LINES_ESP)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    public final RenderType LINES_TRANSLUCENT_DEPTH = RenderType.create(
            "lines-translucent-depth",
            RenderSetup.builder(CustomRenderPipelines.LINES_TRANSLUCENT_DEPTH)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    public final RenderType LINES_TRANSLUCENT_ESP = RenderType.create(
            "lines-translucent-esp",
            RenderSetup.builder(CustomRenderPipelines.LINES_TRANSLUCENT_ESP)
                    .createRenderSetup()
    );

    // RenderTypes.DEBUG_FILLED_BOX / RenderTypes.debugFilledBox() || QUADS_OPAQUE / QUADS_TRANSLUCENT / QUADS_ESP / QUADS_TRANSLUCENT_ESP

    public final RenderType QUADS_OPAQUE = RenderType.create(
            "quads-opaque",
            RenderSetup.builder(CustomRenderPipelines.QUADS_OPAQUE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload()
                    .createRenderSetup()
    );

    public final RenderType QUADS_TRANSLUCENT = RenderType.create(
            "quads-translucent",
            RenderSetup.builder(CustomRenderPipelines.QUADS_TRANSLUCENT)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload()
                    .createRenderSetup()
    );

    public final RenderType QUADS_ESP = RenderType.create(
            "quads-esp",
            RenderSetup.builder(CustomRenderPipelines.QUADS_ESP)
                    .sortOnUpload()
                    .createRenderSetup()
    );

    public final RenderType QUADS_TRANSLUCENT_ESP = RenderType.create(
            "quads-translucent-esp",
            RenderSetup.builder(CustomRenderPipelines.QUADS_TRANSLUCENT_ESP)
                    .sortOnUpload()
                    .createRenderSetup()
    );
}
