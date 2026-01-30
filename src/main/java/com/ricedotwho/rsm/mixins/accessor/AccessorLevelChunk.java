package com.ricedotwho.rsm.mixins.accessor;

import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelChunk.class)
public interface AccessorLevelChunk {
    @Accessor("loaded")
    boolean isLoaded();
}
