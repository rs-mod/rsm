package com.ricedotwho.rsm.event.impl.render;

import com.ricedotwho.rsm.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

@Getter
@AllArgsConstructor
public class Render2DEvent extends Event {
    private final GuiGraphics gfx;
    private final DeltaTracker deltaTracker;
}
