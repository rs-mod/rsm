package com.ricedotwho.rsm.event.impl.render;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class RenderEvent extends Event {
    public static class Render2D extends RenderEvent {

    }

    @Getter
    @AllArgsConstructor
    public static class Extract extends RenderEvent {
        private final WorldExtractionContext context;
    }

    @Getter
    @AllArgsConstructor
    public static class Last extends RenderEvent {
        private final WorldRenderContext context;
    }
}
