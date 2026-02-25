package com.ricedotwho.rsm.module.impl.render.trails;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.module.impl.render.Trail;
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TickTrail extends Trail {
    private static final List<Vec3> TRAIL_LIST = new ArrayList<>();

    public static void onTick(){
        int trailLengthInt = trailLength.getValue().intValue();
        if(TRAIL_LIST.size() >= trailLengthInt) TRAIL_LIST.removeLast();
        TRAIL_LIST.addFirst(playerPos());
    }

    public static void renderBox(){
        float trailWidthFloat = trailWidth.getValue().floatValue() * .1f;
        for (Vec3 trailPos : TRAIL_LIST) {
            Renderer3D.addTask(new Circle(trailPos, false, trailWidthFloat, getColour(), 12));
        }
    }
}
