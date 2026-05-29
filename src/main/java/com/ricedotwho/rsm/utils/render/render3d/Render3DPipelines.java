package com.ricedotwho.rsm.utils.render.render3d;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.RenderPipelines;

@UtilityClass
public final class Render3DPipelines {
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
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
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
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );
}
