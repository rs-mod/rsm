package com.ricedotwho.rsm.mixins.accessor;

import com.ricedotwho.rsm.module.impl.render.Freecam;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public class MixinVisGraph {
    @Inject(at = @At("HEAD"), method = "setOpaque", cancellable = true)
    private void onMarkClosed(BlockPos blockPos, CallbackInfo ci) {
        if (Freecam.isDetached()) ci.cancel();
    }
}
