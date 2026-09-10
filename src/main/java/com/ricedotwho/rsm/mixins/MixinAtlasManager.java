package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(AtlasManager.class)
public class MixinAtlasManager {

    @WrapOperation(method = "lambda$updateSpriteMaps$0", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"))
    private static void silence(Logger instance, String s, Object[] objects, Operation<Void> original) {
        // no-op
    }
}
