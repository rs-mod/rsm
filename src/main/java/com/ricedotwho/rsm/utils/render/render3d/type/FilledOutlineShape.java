package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FilledOutlineShape extends RenderTask {
    private final BlockPos pos;
    private final VoxelShape shape;
    private final Colour fill;
    private final Colour line;
    private final float width;

    public FilledOutlineShape(BlockPos pos, VoxelShape shape, Colour fill, Colour line, boolean depth) {
        this(pos, shape, fill, line, depth, 3f);
    }

    public FilledOutlineShape(BlockPos pos, VoxelShape shape, Colour fill, Colour line, boolean depth, float width) {
        super(RenderType.FILLED_OUTLINE, depth);
        this.pos = pos;
        this.shape = shape;
        this.fill = fill;
        this.line = line;
        this.width = width;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {

        if (source.equals(RenderType.LINE)) {
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                VertexRenderer.renderOutlineBox(
                        stack.last(),
                        buffer,
                        pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                        this.line,
                        this.width
                );
            });
        } else {
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                VertexRenderer.addFilledBoxVertices(
                        stack.last(),
                        buffer,
                        pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                        this.fill
                );
            });
        }
    }
}