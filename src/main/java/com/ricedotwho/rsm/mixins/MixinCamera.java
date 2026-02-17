package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Camera.class)
public abstract class MixinCamera {

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private void onGetPosition(CallbackInfoReturnable<Vec3> cir) {
        Ether ether = RSM.getModule(Ether.class);
        if (ether != null && ether.isEnabled()) {
            Vec3 pos = ether.getCameraPos();
            if (pos == null) return;
            cir.setReturnValue(pos);
        }
    }
}
