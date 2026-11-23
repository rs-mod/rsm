package com.ricedotwho.rsm.type;

import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public enum RenderMode {
    FILLED_OUTLINE(Render3DEvent.Extract::filledOutline),
    OUTLINE((event, box, fill, outline, depth) -> event.outline(box, outline, depth)),
    FILLED((event, box, fill, outline, depth) -> event.filled(box, fill, depth));

    private final Draw draw;
    RenderMode(Draw draw) {
        this.draw = draw;
    }

    public void draw(Render3DEvent.Extract event, AABB box, Color fill, Color outline, boolean depth) {
        this.draw.draw(event, box, fill, outline, depth);
    }

    public void draw(Render3DEvent.Extract event, BlockPos pos, Color fill, Color outline, boolean depth) {
        this.draw.draw(event, new AABB(pos), fill, outline, depth);
    }

    public interface Draw {
        void draw(Render3DEvent.Extract event, AABB box, Color fill, Color outline, boolean depth);
    }
}
