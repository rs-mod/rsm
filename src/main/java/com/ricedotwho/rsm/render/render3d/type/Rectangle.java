package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.world.phys.AABB;

public class Rectangle extends RenderTask {
    private final AABB aabb;
    private final float lineWidth;
    private final Color color;

    public Rectangle(AABB aabb, Color color, boolean depth) {
        this(aabb, color, 3f, depth);
    }

    public Rectangle(AABB aabb, Color color, float lineWidth, boolean depth) {
        super(RenderType.LINE, depth);
        this.aabb = aabb;
        this.lineWidth = lineWidth;
        this.color = color;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderHorizontalRect(
                stack.last(),
                buffer,
                this.aabb,
                this.lineWidth,
                this.color
        );
    }
}
