package com.ricedotwho.rsm.utils.render.render3d;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

@UtilityClass
public final class Render3DLayer {
    public final RenderType.CompositeRenderType LINE_LIST = RenderType.create(
            "line-list",
            RenderType.BIG_BUFFER_SIZE,
            Render3DPipelines.LINE_LIST,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
                    .createCompositeState(false)
    );

    public final RenderType.CompositeRenderType LINE_LIST_ESP = RenderType.create(
            "line-list-esp",
            RenderType.BIG_BUFFER_SIZE,
            Render3DPipelines.LINE_LIST_ESP,
            RenderType.CompositeState
                    .builder()
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
                    .createCompositeState(false)
    );

    public final RenderType.CompositeRenderType TRIANGLE_STRIP = RenderType.create(
            "triangle_strip",
            RenderType.BIG_BUFFER_SIZE,
            false,
            true,
            Render3DPipelines.TRIANGLE_STRIP,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)
    );

    public final RenderType.CompositeRenderType TRIANGLE_STRIP_ESP = RenderType.create(
            "triangle_strip_esp",
            RenderType.BIG_BUFFER_SIZE,
            false,
            true,
            Render3DPipelines.TRIANGLE_STRIP_ESP,
            RenderType.CompositeState.builder().createCompositeState(false)
    );
}
