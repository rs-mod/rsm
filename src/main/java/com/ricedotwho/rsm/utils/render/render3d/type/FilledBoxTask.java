package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import lombok.Getter;
import net.minecraft.world.phys.AABB;

@Getter
public class FilledBoxTask extends RenderTask {
    private final AABB aabb;
    private final Colour colour;

    public FilledBoxTask(AABB aabb, Colour colour, boolean depth) {
        super(RenderType.BOX_FILLED, depth);
        this.aabb = aabb;
        this.colour = colour;
    }
}