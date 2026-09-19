package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.impl.player.Chat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Inject(method = "extractPingIcon", at = @At("HEAD"), cancellable = true)
    private void removePingInSkyblock(GuiGraphicsExtractor graphics, int slotWidth, int xo, int yo, PlayerInfo info, CallbackInfo ci) {
        if (!Location.isInSkyblock() || !Chat.getInstance().getHidePing().getValue()) return;
        ci.cancel();
    }
}
