package com.ricedotwho.rsm.module.impl.render.trails;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.module.impl.render.Trail;
import com.ricedotwho.rsm.utils.render.render3d.type.LineList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record LineTrail(Trail config) {
    private static final List<Vec3> TRAIL_SEGMENTS = new ArrayList<>();

    public void onMovementPacket() {
        if (!TRAIL_SEGMENTS.isEmpty() && TRAIL_SEGMENTS.getLast().equals(config.playerPos())) return;
        if (TRAIL_SEGMENTS.size() >= config.getTrailLength().getValue().intValue()) TRAIL_SEGMENTS.removeLast();
        TRAIL_SEGMENTS.addFirst(config.playerPos());
    }

    public void renderTrail() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Renderer3D.addTask(new LineList(TRAIL_SEGMENTS, config.getColour().getValue(), config.getEndColour().getValue(), config.getDepth().getValue()));
    }

    public void reset() {
        TRAIL_SEGMENTS.clear();
    }
}
