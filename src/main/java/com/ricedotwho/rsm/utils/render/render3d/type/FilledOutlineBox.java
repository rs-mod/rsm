package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import lombok.Getter;
import net.minecraft.world.phys.AABB;

@Getter
public class FilledOutlineBox extends RenderTask {
    private final AABB aabb;
    private final Colour fill;
    private final Colour line;

    public FilledOutlineBox(AABB aabb, Colour fill, Colour line, boolean depth) {
        super(RenderType.BOX_FILLED_OUTLINE, depth);
        this.aabb = aabb;
        this.fill = fill;
        this.line = line;
    }
}