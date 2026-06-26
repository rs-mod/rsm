package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OutlineShape extends RenderTask {
    private final BlockPos pos;
    private final VoxelShape shape;
    private final Colour colour;
    private final float width;

    public OutlineShape(BlockPos pos, VoxelShape shape, Colour colour, boolean depth) {
        this(pos, shape, colour, depth, 3f);
    }

    public OutlineShape(BlockPos pos, VoxelShape shape, Colour colour, boolean depth, float width) {
        super(RenderType.LINE, depth);
        this.pos = pos;
        this.shape = shape;
        this.colour = colour;
        this.width = width;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            VertexRenderer.renderOutlineBox(
                    stack.last(),
                    buffer,
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                    this.colour,
                    this.width
            );
        });
    }
}