package com.ricedotwho.rsm.event.impl.world;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Getter
public class
BlockChangeEvent extends Event {
    private final BlockPos blockPos;
    private final Vec3 vec3;
    private final BlockState newState;
    private final BlockState oldState;

    public BlockChangeEvent(BlockPos blockPos, BlockState newState) {
        this.blockPos = blockPos;
        this.vec3 = new Vec3(blockPos);
        this.newState = newState;
        this.oldState = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getBlockState(blockPos);
    }
}
