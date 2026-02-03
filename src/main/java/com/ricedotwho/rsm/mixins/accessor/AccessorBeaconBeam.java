package com.ricedotwho.rsm.mixins.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconRenderer.class)
public interface AccessorBeaconBeam {
    @Invoker("submitBeaconBeam")
    static void renderBeaconBeam(PoseStack matrices, SubmitNodeCollector queue, ResourceLocation textureId, float beamHeight, float beamRotationDegrees, int minHeight, int maxHeight, int color, float innerScale, float outerScale) {
        throw new UnsupportedOperationException();
    }
}
