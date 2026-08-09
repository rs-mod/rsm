package com.ricedotwho.rsm.mixins;

import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(YggdrasilServicesKeyInfo.class)
public class MixinYggdrasilServicesKeyInfo {

    @Redirect(method = "validateProperty", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void silence(Logger instance, String s, Object object, Object object2) {
        // no-op
    }
}
