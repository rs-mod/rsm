package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.dungeon.BarFix;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel {
    @Inject(method = "syncBlockState", at = @At("TAIL"))
    public void syncBlockStateTail(BlockPos pos, BlockState state, Vec3 playerPos, CallbackInfo ci) {
        BarFix.postSync(pos, state);
    }
}
