package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.dungeon.BarFix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(CrossCollisionBlock.class)
public class MixinCrossCollisionBlock {
    @Final
    @Shadow
    private Function<BlockState, VoxelShape> collisionShapes;

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    protected void getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if ((CrossCollisionBlock) (Object) this instanceof IronBarsBlock && BarFix.test(state, false)) {
            cir.setReturnValue(this.collisionShapes.apply(BarFix.getState(state)));
        }
    }
}
