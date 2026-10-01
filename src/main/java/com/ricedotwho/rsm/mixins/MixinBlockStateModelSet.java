package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.dungeon.BarFix;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockStateModelSet.class)
public class MixinBlockStateModelSet {

    @Shadow
    @Final
    private Map<BlockState, BlockStateModel> modelByState;

    @Shadow
    @Final
    private BlockStateModel missingModel;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void replaceIronBars(BlockState state, CallbackInfoReturnable<BlockStateModel> cir) {
        if (!BarFix.getInstance().getReplaceVisually().getValue() || !(state.getBlock() instanceof IronBarsBlock) || !BarFix.test(state, false)) return;
        cir.setReturnValue(this.modelByState.getOrDefault(BarFix.getState(state), this.missingModel));
    }
}
