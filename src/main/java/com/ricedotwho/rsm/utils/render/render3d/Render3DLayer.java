package com.ricedotwho.rsm.utils.render.render3d;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

@UtilityClass
public final class Render3DLayer {
    public final RenderType LINE_LIST = RenderType.create(
            "line-list",
            RenderSetup.builder(Render3DPipelines.LINE_LIST)
                    .bufferSize(RenderType.BIG_BUFFER_SIZE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    public final RenderType LINE_LIST_ESP = RenderType.create(
            "line-list-esp",
            RenderSetup.builder(Render3DPipelines.LINE_LIST_ESP)
                    .bufferSize(RenderType.BIG_BUFFER_SIZE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    public final RenderType TRIANGLE_STRIP = RenderType.create(
            "triangle_strip",
            RenderSetup.builder(Render3DPipelines.TRIANGLE_STRIP)
                    .bufferSize(RenderType.BIG_BUFFER_SIZE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload()
                    .createRenderSetup()
    );

    public final RenderType TRIANGLE_STRIP_ESP = RenderType.create(
            "triangle_strip_esp",
            RenderSetup.builder(Render3DPipelines.TRIANGLE_STRIP_ESP)
                    .bufferSize(RenderType.BIG_BUFFER_SIZE)
                    .sortOnUpload()
                    .createRenderSetup()
    );
}
