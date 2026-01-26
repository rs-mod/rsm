package com.ricedotwho.rsm.event.impl.render;


import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.DeltaTracker;

@Getter
@AllArgsConstructor
public class Render3DEvent extends Event {
    private final DeltaTracker deltaTracker;
}
