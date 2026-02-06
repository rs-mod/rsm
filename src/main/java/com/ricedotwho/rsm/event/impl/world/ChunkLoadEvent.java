package com.ricedotwho.rsm.event.impl.world;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.level.chunk.LevelChunk;

@Getter
@AllArgsConstructor
public class ChunkLoadEvent extends Event {
    private LevelChunk chunk;
}
