package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.render.HidePlayers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void hideStuckArrows(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (!HidePlayers.getInstance().isEnabled() || !HidePlayers.getInstance().getHideStuckArrows().getValue()) return;
        state.arrowCount = 0;
    }
}
