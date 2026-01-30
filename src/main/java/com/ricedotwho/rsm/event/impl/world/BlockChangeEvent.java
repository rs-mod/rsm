package com.ricedotwho.rsm.event.impl.world;

import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Getter
public class BlockChangeEvent extends Event {
    private final BlockPos blockPos;
    private final Pos pos;
    private BlockState newState;
    private final BlockState oldState;

    public BlockChangeEvent(BlockPos blockPos, BlockState newState) {
        this.blockPos = blockPos;
        this.pos = new Pos(blockPos);
        this.newState = newState;
        this.oldState = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getBlockState(blockPos);
    }
}
