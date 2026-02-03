package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
public class LineTask extends RenderTask {
    private final Vec3 from;
    private final Vec3 to;
    private final Colour start;
    private final Colour end;

    public LineTask(Vec3 from, Vec3 to, Colour start, Colour end, boolean depth) {
        super(RenderType.LINE, depth);
        this.from = from;
        this.to = to;
        this.start = start;
        this.end = end;
    }
}