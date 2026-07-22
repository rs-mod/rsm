package com.ricedotwho.rsm.mixins;

import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.module.impl.render.BreakingTexture;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.renderer.QuadConsumers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.*;

// fuck you fabric api
@Mixin(value = BlockFeatureRenderer.class, priority = 1100)
public class MixinBlockFeatureRenderer {
    @Shadow
    @Final
    private RandomSource random;

    /**
     * @reason fuck fabric api
     * @author .
     */
    @Overwrite
    private void renderBreakingBlockModelSubmits(final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource) {
        QuadConsumers.BreakingBlockModel quadConsumer = new QuadConsumers.BreakingBlockModel();
        QuadEmitter output = Renderer.get().quadEmitter(quadConsumer);

        for (SubmitNodeStorage.BreakingBlockModelSubmit submit : nodeCollection.getBreakingBlockModelSubmits()) {
            VertexConsumer buffer = new SheetedDecalTextureGenerator(bufferSource.getBuffer(getDestroyType(submit.progress())), submit.pose(), 2F);
            quadConsumer.pose = submit.pose();
            quadConsumer.buffer = buffer;
            output.clear();
            random.setSeed(submit.seed());
            // TODO FRAPI 26.1: somehow pass the level, pos, and state here when available? maybe via extended submit type?
            submit.model().emitQuads(output, BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, _ -> false);
        }
    }

    @Unique
    public RenderType getDestroyType(int progress) {
        if (BreakingTexture.INSTANCE.isEnabled()) {
            return BreakingTexture.DESTROY_TYPES.get(progress);
        }
        return ModelBakery.DESTROY_TYPES.get(progress);
    }
}
