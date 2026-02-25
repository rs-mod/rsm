package com.ricedotwho.rsm.module.impl.render.trails;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.module.impl.render.Trail;
import com.ricedotwho.rsm.utils.render.render3d.type.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LineTrail extends Trail {
    private static final List<Vec3> TRAIL_SEGMENTS = new ArrayList<>();

    public static void registerTrailPos(){
        if(playerPos() == playerPosOld()) return;
        int trailLengthInt = trailLength.getValue().intValue() * 5;
        if(TRAIL_SEGMENTS.size() >= trailLengthInt) TRAIL_SEGMENTS.removeLast();
        TRAIL_SEGMENTS.addFirst(playerPos());
    }

    public static void renderTrail(){
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        for (int i = 0; i < TRAIL_SEGMENTS.size()-1; i++) {
            Renderer3D.addTask(new Line(TRAIL_SEGMENTS.get(i), TRAIL_SEGMENTS.get((i + 1)), getColour(), getColour(), false));
        }
    }
}
