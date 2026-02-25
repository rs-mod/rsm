package com.ricedotwho.rsm.module.impl.render.trails;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.render.Trail;
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import lombok.AllArgsConstructor;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record TickTrail(Trail config) {
    private static final List<Vec3> TRAIL_LIST = new ArrayList<>();

    public void onTick() {
        if (TRAIL_LIST.size() >= config.getTrailLength().getValue().intValue()) TRAIL_LIST.removeLast();
        TRAIL_LIST.addFirst(config.playerPos());
    }

    public void renderBox() {
        float trailWidthFloat = config.getTrailWidth().getValue().floatValue() * .1f;
        for (Vec3 trailPos : TRAIL_LIST) {
            Renderer3D.addTask(new Circle(trailPos, config.getDepth().getValue(), trailWidthFloat, config.getColour().getValue(), 12));
        }
    }
}
