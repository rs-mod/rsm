package com.ricedotwho.rsm.utils.render.render3d;

import com.mojang.blaze3d.pipeline.BlendFunction;
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
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
                    .withCull(false).withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(true)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build()
    );

    public final RenderPipeline LINE_LIST_ESP = RenderPipelines.register(
            RenderPipeline.builder(
                    RenderPipelines.LINES_SNIPPET)
                    .withLocation("pipeline/lines")
                    .withShaderDefine("shad")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
                    .withCull(false).withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );

    public final RenderPipeline TRIANGLE_STRIP = RenderPipelines.register(
            com.mojang.blaze3d.pipeline.RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/debug_filled_box").withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withDepthWrite(true).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build()
    );

    public final RenderPipeline TRIANGLE_STRIP_ESP = RenderPipelines.register(
            RenderPipeline.builder(
                    RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/debug_filled_box")
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build()
    );
}
