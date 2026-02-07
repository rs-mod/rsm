package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
public class Beacon extends RenderTask implements Accessor {
    private final Vec3 pos;
    private final Colour colour;
    private final boolean scoping;
    private final long gameTime;

    @Deprecated
    public Beacon(Pos pos, Colour colour) {
        this(pos.asVec3(), colour);
    }

    public Beacon(Vec3 pos, Colour colour) {
        super(RenderType.BEACON, false);
        this.pos = pos;
        this.colour = colour;
        if (mc.level == null || mc.player == null) {
            this.scoping = false;
            this.gameTime = 0L;
        } else {
            this.scoping = mc.player.isScoping();
            this.gameTime = mc.level.getGameTime();
        }
    }
}