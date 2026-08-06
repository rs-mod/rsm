package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.module.impl.dungeon.BarFix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minecraft.world.level.block.CrossCollisionBlock.*;

@Mixin(CrossCollisionBlock.class)
public class MixinCrossCollisionBlock {
    @Final
    @Shadow
    private Function<BlockState, VoxelShape> collisionShapes;
    @Final
    @Shadow
    private Function<BlockState, VoxelShape> shapes;

    @Unique
    private final Map<Block, BlockState> ALL_DIRECTION_STATE = new HashMap<>();


    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    protected void getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (BarFix.test(state, false)) {
            cir.setReturnValue(this.shapes.apply(getState()));
        }
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    protected void getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (BarFix.test(state, false)) {
            cir.setReturnValue(this.collisionShapes.apply(getState()));
        }
    }

    @Unique
    private BlockState getState() {
        return ALL_DIRECTION_STATE.computeIfAbsent((CrossCollisionBlock) (Object) this, k -> k.defaultBlockState().setValue(NORTH, true).setValue(SOUTH, true).setValue(EAST, true).setValue(WEST, true));
    }
}
