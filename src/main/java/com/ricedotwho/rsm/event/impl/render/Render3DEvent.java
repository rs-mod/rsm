package com.ricedotwho.rsm.event.impl.render;


import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;

public class Render3DEvent extends Event {
    @Getter
    @AllArgsConstructor
    public static class Extract extends Render3DEvent {
        private final WorldExtractionContext context;
    }

    @Getter
    @AllArgsConstructor
    public static class Last extends Render3DEvent {
        private final WorldRenderContext context;
    }

    @Getter
    @AllArgsConstructor
    public static class Start extends Render3DEvent {
        private final WorldTerrainRenderContext context;
    }
}
