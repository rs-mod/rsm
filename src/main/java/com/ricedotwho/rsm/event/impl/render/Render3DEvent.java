package com.ricedotwho.rsm.event.impl.render;


import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;

public sealed abstract class Render3DEvent extends Event {
    @Getter
    @AllArgsConstructor
    public final static class Extract extends Render3DEvent {
        private final LevelExtractionContext context;
    }

    @Getter
    @AllArgsConstructor
    public final static class Last extends Render3DEvent {
        private final LevelRenderContext context;
    }

    @Getter
    @AllArgsConstructor
    public final static class Start extends Render3DEvent {
        private final LevelTerrainRenderContext context;
    }
}
