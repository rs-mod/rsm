package com.ricedotwho.rsm.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.ricedotwho.rsm.render.render2d.RoundedRectRenderer;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/CustomRenderPipelines.kt)
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
public final class CustomRenderPipelines {
    public final DepthStencilState NO_DEPTH = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
    private final DepthStencilState DEPTH_WRITE = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true);
    private final ColorTargetState TRANSLUCENT = new ColorTargetState(BlendFunction.TRANSLUCENT);
    
    public final RenderPipeline LINES_TRANSLUCENT_DEPTH = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(DEPTH_WRITE)
            .withColorTargetState(TRANSLUCENT)
            .withLocation("rsm/lines_translucent_depth")
            .build()
    );

    public final RenderPipeline LINES_ESP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withLocation("rsm/lines_esp")
            .build()
    );

    public final RenderPipeline LINES_TRANSLUCENT_ESP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withColorTargetState(TRANSLUCENT)
            .withLocation("rsm/lines_translucent_esp")
            .build()
    );

    public final RenderPipeline QUADS_OPAQUE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(DEPTH_WRITE)
            .withCull(false)
            .withLocation("rsm/quads_opaque")
            .build()
    );

    public final RenderPipeline QUADS_ESP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withCull(false)
            .withLocation("rsm/quads_esp")
            .build()
    );

    public final RenderPipeline QUADS_TRANSLUCENT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(DEPTH_WRITE)
            .withColorTargetState(TRANSLUCENT)
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withLocation("rsm/quads_translucent")
            .build()
    );

    public final RenderPipeline QUADS_TRANSLUCENT_ESP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withDepthStencilState(NO_DEPTH)
            .withColorTargetState(TRANSLUCENT)
            .withCull(false)
            .withLocation("rsm/quads_translucent_esp")
            .build()
    );

    public final RenderPipeline UI_TEXT = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("rsm", "pipeline/ui_text"))
            .withVertexShader(Identifier.fromNamespaceAndPath("rsm", "core/text"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("rsm", "core/text"))
            .withVertexBinding(0, Fonts.FORMAT)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    public final RenderPipeline ROUNDED_RECT = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("rsm", "pipeline/round_rect"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("rsm", "core/round_rect"))
            .withVertexShader(Identifier.fromNamespaceAndPath("rsm", "core/round_rect"))
            .withVertexBinding(0, RoundedRectRenderer.FORMAT)
            .build();

    public final RenderPipeline ROUNDED_RECT_TEXTURED = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("rsm", "pipeline/round_rect_textured"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("rsm", "core/round_rect_textured"))
            .withVertexShader(Identifier.fromNamespaceAndPath("rsm", "core/round_rect"))
            .withVertexBinding(0, RoundedRectRenderer.FORMAT)
            .build();

    public final RenderPipeline ROUNDED_RECT_SHADOW = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("rsm", "pipeline/round_rect_shadow"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("rsm", "core/round_rect_shadow"))
            .withVertexShader(Identifier.fromNamespaceAndPath("rsm", "core/round_rect"))
            .withVertexBinding(0, RoundedRectRenderer.FORMAT)
            .build();
}
