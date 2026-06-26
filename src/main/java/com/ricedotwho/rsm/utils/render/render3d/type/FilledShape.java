package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FilledShape extends RenderTask {
    private final BlockPos pos;
    private final VoxelShape shape;
    private final Colour colour;

    public FilledShape(BlockPos pos, VoxelShape shape, Colour colour, boolean depth) {
        super(RenderType.FILLED, depth);
        this.pos = pos;
        this.shape = shape;
        this.colour = colour;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            VertexRenderer.addFilledBoxVertices(
                    stack.last(),
                    buffer,
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                    this.colour
            );
        });
    }
}