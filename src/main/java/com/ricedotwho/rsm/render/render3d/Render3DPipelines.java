package com.ricedotwho.rsm.render.render3d;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendEquation;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.BlendOp;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

@UtilityClass
public final class Render3DPipelines {
    public final DepthStencilState NO_DEPTH = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
    //private final VertexFormat BLOCK_NO_COLOUR = VertexFormat.builder().add("Position",VertexFormatElement.POSITION).add("UV0",VertexFormatElement.UV0).add("UV2",VertexFormatElement.UV2).build();

    public final RenderPipeline LINE_LIST = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation("pipeline/lines")
                    .build()
    );

    public final RenderPipeline LINE_LIST_ESP = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation("pipeline/lines_esp")
                    .withDepthStencilState(NO_DEPTH)
                    .build()
    );

    public final RenderPipeline TRIANGLE_STRIP = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation("pipeline/debug_filled_box")
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
                    .build()
    );

    public final RenderPipeline TRIANGLE_STRIP_ESP = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation("pipeline/debug_filled_box_esp")
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
                    .withDepthStencilState(NO_DEPTH)
                    .build()
    );

    public static final RenderPipeline CRUMBLING = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation("pipeline/crumbling_rsm")
                    .withVertexShader("core/rendertype_crumbling")
                    .withFragmentShader(Identifier.fromNamespaceAndPath("rsm", "crumbling"))
                    .withColorTargetState(new ColorTargetState(new BlendFunction(
                            new BlendEquation(BlendFactor.ONE, BlendFactor.ZERO, BlendOp.ADD),
                            new BlendEquation(BlendFactor.ONE, BlendFactor.ZERO, BlendOp.ADD))))
                    //.withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO)))
                    .withVertexBinding(0, DefaultVertexFormat.BLOCK)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false, -1.0F, -10.0F))
                    .build()
    );
}