package com.ricedotwho.rsm.render.render3d;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

@UtilityClass
public final class Render3DPipelines {
    public final DepthStencilState NO_DEPTH = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
    //private final VertexFormat BLOCK_NO_COLOUR = VertexFormat.builder().add("Position",VertexFormatElement.POSITION).add("UV0",VertexFormatElement.UV0).add("UV2",VertexFormatElement.UV2).build();

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

    public static final RenderPipeline CRUMBLING = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation("pipeline/crumbling_rsm")
                    .withVertexShader("core/rendertype_crumbling")
                    .withFragmentShader(Identifier.fromNamespaceAndPath("rsm", "crumbling"))
                    .withSampler("Sampler0")
                    .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO)))
                    //.withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO)))
                    .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false, -1.0F, -10.0F))
                    .build()
    );
}
