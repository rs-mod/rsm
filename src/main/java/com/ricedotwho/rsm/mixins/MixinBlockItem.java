package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.player.NoPlace;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// From [NoammAddons](https://github.com/Noamm9/NoammAddons/blob/1.21.11/src/main/java/com/github/noamm9/mixin/MixinBlockItem.java)

@Mixin(BlockItem.class)
public class MixinBlockItem {
    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void placeBlockHook(BlockPlaceContext context, BlockState placementState, CallbackInfoReturnable<Boolean> cir) {
        if (NoPlace.doBlockPlace(context)) cir.setReturnValue(true);
    }

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"), cancellable = true)
    private void useHook(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (NoPlace.doBlockPlace(placeContext)) cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
