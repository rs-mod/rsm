package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class RenderTask {
    protected final RenderType type;
    protected final boolean depth;

    public abstract void render(PoseStack stack, VertexConsumer buffer, RenderType source);
}