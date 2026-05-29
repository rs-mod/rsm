package com.ricedotwho.rsm.utils.render.render3d;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.RenderPipelines;

@UtilityClass
public final class Render3DPipelines {
    public static final DepthStencilState NO_DEPTH = new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    public final RenderPipeline LINE_LIST = RenderPipelines.register(
            RenderPipeline.builder(
                    RenderPipelines.LINES_SNIPPET)
                    .withLocation("pipeline/lines")
                    .build()
    );

    public final RenderPipeline LINE_LIST_ESP = RenderPipelines.register(
            RenderPipeline.builder(
                    RenderPipelines.LINES_SNIPPET)
                    .withLocation("pipeline/lines")
                    .withDepthStencilState(NO_DEPTH)
                    .build()
    );

    public final RenderPipeline TRIANGLE_STRIP = RenderPipelines.register(
            RenderPipeline.builder(
                    RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/debug_filled_box")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .build()
    );

    public final RenderPipeline TRIANGLE_STRIP_ESP = RenderPipelines.register(
            RenderPipeline.builder(
                    RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/debug_filled_box")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withDepthStencilState(NO_DEPTH)
                    .build()
    );
}
