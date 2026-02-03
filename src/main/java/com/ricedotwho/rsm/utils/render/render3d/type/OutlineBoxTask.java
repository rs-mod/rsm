package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import lombok.Getter;
import net.minecraft.world.phys.AABB;

@Getter
public class OutlineBoxTask extends RenderTask {
    private final AABB aabb;
    private final Colour colour;

    public OutlineBoxTask(AABB aabb, Colour colour, boolean depth) {
        super(RenderType.BOX_OUTLINE, depth);
        this.aabb = aabb;
        this.colour = colour;
    }
}