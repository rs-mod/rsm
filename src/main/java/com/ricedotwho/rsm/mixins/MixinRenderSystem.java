package com.ricedotwho.rsm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.ricedotwho.rsm.core.GeneratedGraphicsInitList;
import com.ricedotwho.rsm.core.GraphicsInit;
import com.ricedotwho.rsm.core.RSM;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
    @Inject(method = "initRenderer", at = @At("TAIL"))
    private static void rsm$initRenderer(GpuDevice device, CallbackInfo ci) {
        GeneratedGraphicsInitList.initClasses.forEach(it -> RSM.getInstance().initClass(it, GraphicsInit.class));
    }
}
