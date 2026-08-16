package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class FilledBox extends RenderTask {
    private final AABB aabb;
    private final int color;

    public FilledBox(AABB aabb, int color, boolean depth) {
        super(RenderType.FILLED, depth);
        this.aabb = aabb;
        this.color = color;
    }

    public FilledBox(BlockPos pos, int color, boolean depth) {
        super(RenderType.FILLED, depth);
        this.aabb = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        this.color = color;
    }

    public FilledBox(AABB aabb, Color color, boolean depth) {
        this(aabb, color.getARGB(), depth);
    }

    public FilledBox(BlockPos pos, Color color, boolean depth) {
        this(pos, color.getARGB(), depth);
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.addFilledBoxVertices(
                stack.last(),
                buffer,
                this.aabb,
                this.color
        );
    }
}