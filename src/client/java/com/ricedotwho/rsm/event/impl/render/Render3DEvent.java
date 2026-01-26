package com.ricedotwho.rsm.event.impl.render;


import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Render3DEvent extends Event {
    private final float partialTicks;
}
