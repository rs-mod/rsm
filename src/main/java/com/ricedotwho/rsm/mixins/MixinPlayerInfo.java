package com.ricedotwho.rsm.mixins;

import com.mojang.authlib.GameProfile;
import com.ricedotwho.rsm.component.impl.CustomPlayerManager;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(PlayerInfo.class)
public class MixinPlayerInfo {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "createSkinLookup", at = @At("HEAD"))
    private static void loadTextures(GameProfile gameProfile, CallbackInfoReturnable<Supplier<PlayerSkin>> cir) {
        CustomPlayerManager.onLoadTexture(gameProfile);
    }

    @Inject(method = "getSkin", at = @At("TAIL"), cancellable = true)
    private void getCapeTexture(CallbackInfoReturnable<PlayerSkin> cir) {
        if (!ClickGUI.getCapes().getValue()) return;
        CustomPlayerManager.PlayerData handler = CustomPlayerManager.get(profile);
        if (handler.isHasCape()) {
            PlayerSkin oldTextures = cir.getReturnValue();
            ClientAsset.Texture capeTexture = handler.getCape();
            ClientAsset.Texture elytraTexture = handler.isHasElytra() ? capeTexture : new ClientAsset.ResourceTexture(Identifier.parse("textures/entity/equipment/wings/elytra.png"),null);
            PlayerSkin newTextures = new PlayerSkin(
                    oldTextures.body(),
                    capeTexture,
                    elytraTexture,
                    oldTextures.model(),
                    oldTextures.secure()
            );
            cir.setReturnValue(newTextures);
        }
    }
}
